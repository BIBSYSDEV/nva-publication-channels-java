package no.sikt.nva.pubchannels.handler.search.journal;

import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CHANNEL_REGISTRY_PAGE_COUNT_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.CHANNEL_REGISTRY_PAGE_NO_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_OFFSET;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_OFFSET_INT;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_SIZE;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_SIZE_INT;
import static no.sikt.nva.pubchannels.TestConstants.ISSN_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.NAME_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.TestConstants.YEAR_QUERY_PARAM;
import static no.sikt.nva.pubchannels.handler.TestUtils.areEqualURIs;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchResponseBody;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchResult;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.net.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.model.JournalDto;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandlerTest;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.UnprocessableContentException;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@WireMockTest(httpsEnabled = true)
class SearchJournalByQueryHandlerTest extends SearchByQueryHandlerTest {

    private static final URI SELF_URI_BASE = UriWrapper.fromHost(API_DOMAIN)
                                                       .addChild(CUSTOM_DOMAIN_BASE_PATH)
                                                       .addChild(JOURNAL_PATH)
                                                       .getUri();
    private static final Context context = new FakeContext();
    private static final TypeReference<PaginatedSearchResult<JournalDto>> TYPE_REF = new TypeReference<>() {
    };

    @Override
    protected String getPath() {
        return ChannelType.JOURNAL.pathElement;
    }

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {

        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);
        var channelRegistryBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);

        this.handlerUnderTest = new SearchJournalByQueryHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @ParameterizedTest
    @DisplayName("Should return requested media type")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType)
        throws IOException, UnprocessableContentException {
        var year = randomYear();
        var issn = randomIssn();
        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedSearchResult = getExpectedPaginatedSearchResultIssnSearch(year, issn);

        var input = constructRequest(Map.of("year", String.valueOf(year), "query", issn), mediaType);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
        var contentType = response.getHeaders().get(CONTENT_TYPE);
        assertThat(contentType, is(equalTo(expectedMediaType)));
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsIssn() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var issn = randomIssn();
        var expectedSearchResult = getExpectedPaginatedSearchResultIssnSearch(year, issn);

        var input = constructRequest(Map.of("year", String.valueOf(year), "query", issn), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithRequestedYearIfThirdPartyDoesNotProvideYear()
        throws IOException, UnprocessableContentException {
        var year = randomYear();
        var issn = randomIssn();
        var expectedSearchResult = getExpectedPaginatedSearchResultIssnSearchThirdPartyDoesNotProvideYear(year, issn);

        var input = constructRequest(Map.of("year", String.valueOf(year), "query", issn), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsName() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var yearString = String.valueOf(year);
        var name = randomString();
        int maxNr = 30;
        int offset = 0;
        int size = 10;
        var channelRegistrySearchResult = getChannelRegistrySearchResult(year, name, maxNr);
        var responseBody = getChannelRegistrySearchResponseBody(channelRegistrySearchResult, offset, size);
        stubChannelRegistrySearchResponse(responseBody,
                                          HttpURLConnection.HTTP_OK,
                                          YEAR_QUERY_PARAM,
                                          yearString,
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
                                          DEFAULT_SIZE,
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM,
                                          DEFAULT_OFFSET,
                                          NAME_QUERY_PARAM,
                                          name);
        var input = constructRequest(Map.of("year", yearString, "query", name), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(channelRegistrySearchResult.size())));
        var expectedSearchResult = getExpectedPaginatedSearchJournalResultNameSearch(channelRegistrySearchResult,
                                                                                     yearString,
                                                                                     name,
                                                                                     offset,
                                                                                     size);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryOmitsYear() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var name = randomString();
        int maxNr = 30;
        int offset = 0;
        int size = 10;
        var result = getChannelRegistrySearchResult(year, name, maxNr);
        var responseBody = getChannelRegistrySearchResponseBody(result, offset, size);
        stubChannelRegistrySearchResponse(responseBody,
                                          HttpURLConnection.HTTP_OK,
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
                                          DEFAULT_SIZE,
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM,
                                          DEFAULT_OFFSET,
                                          NAME_QUERY_PARAM,
                                          name);
        var input = constructRequest(Map.of("query", name), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(result.size())));
        var expectedSearchResult = getExpectedPaginatedSearchJournalResultNameSearch(result, null, name, offset, size);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsNameAndOffsetIs10() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var yearString = String.valueOf(year);
        var name = randomString();
        int offset = 10;
        int size = 10;
        int maxNr = 30;
        var channelRegistrySearchResult = getChannelRegistrySearchResult(year, name, maxNr);
        stubChannelRegistrySearchResponse(getChannelRegistrySearchResponseBody(channelRegistrySearchResult,
                                                                               offset,
                                                                               size),
                                          HttpURLConnection.HTTP_OK,
                                          YEAR_QUERY_PARAM,
                                          yearString,
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
                                          String.valueOf(size),
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM,
                                          String.valueOf(offset / size),
                                          NAME_QUERY_PARAM,
                                          name);
        var input = constructRequest(Map.of("year",
                                            yearString,
                                            "query",
                                            name,
                                            "offset",
                                            String.valueOf(offset),
                                            "size",
                                            String.valueOf(size)), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(channelRegistrySearchResult.size())));
        var expectedSearchResult = getExpectedPaginatedSearchJournalResultNameSearch(channelRegistrySearchResult,
                                                                                     yearString,
                                                                                     name,
                                                                                     offset,
                                                                                     size);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithQueryAsId() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var yearString = String.valueOf(year);
        var name = randomString();
        int offset = 10;
        int size = 10;
        int maxNr = 30;
        var channelRegistrySearchResult = getChannelRegistrySearchResult(year, name, maxNr);
        stubChannelRegistrySearchResponse(getChannelRegistrySearchResponseBody(channelRegistrySearchResult,
                                                                               offset,
                                                                               size),
                                          HttpURLConnection.HTTP_OK,
                                          YEAR_QUERY_PARAM,
                                          yearString,
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
                                          String.valueOf(size),
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM,
                                          String.valueOf(offset / size),
                                          NAME_QUERY_PARAM,
                                          name);
        var input = constructRequest(Map.of("year",
                                            yearString,
                                            "query",
                                            name,
                                            "offset",
                                            String.valueOf(offset),
                                            "size",
                                            String.valueOf(size)), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        var expectedSearchResult = getExpectedPaginatedSearchJournalResultNameSearch(channelRegistrySearchResult,
                                                                                     yearString,
                                                                                     name,
                                                                                     offset,
                                                                                     size);
        var expectedUri = expectedSearchResult.getId();
        var actualUri = pagesSearchResult.getId();

        assertTrue(areEqualURIs(actualUri, expectedUri));
    }

    private static PaginatedSearchResult<JournalDto> getExpectedPaginatedSearchJournalResultNameSearch(List<String> channelRegistryResults,
                                                                                                       String year,
                                                                                                       String name,
                                                                                                       int queryOffset,
                                                                                                       int querySize)
        throws UnprocessableContentException {
        var expectedHits = mapToJournalResults(channelRegistryResults, year);
        var expectedParams = new HashMap<String, String>();
        expectedParams.put("query", name);
        if (year != null) {
            expectedParams.put("year", year);
        }

        return PaginatedSearchResult.create(constructPublicationChannelUri(JOURNAL_PATH, null),
                                            queryOffset,
                                            querySize,
                                            expectedHits.size(),
                                            expectedHits.stream().skip(queryOffset).limit(querySize).toList(),
                                            expectedParams);
    }

    private static List<JournalDto> mapToJournalResults(List<String> channelRegistryResults, String requestedYear) {
        return channelRegistryResults.stream()
                                     .map(result -> attempt(() -> objectMapper.readValue(result,
                                                                                         ChannelRegistryJournal.class)).orElseThrow())
                                     .map(journal -> toJournalResult(journal, requestedYear))
                                     .toList();
    }

    private static JournalDto toJournalResult(ThirdPartySerialPublication journal, String requestedYear) {
        return JournalDto.create(constructPublicationChannelUri(JOURNAL_PATH, null), journal, requestedYear);
    }

    private static PaginatedSearchResult<JournalDto> getExpectedSearchResult(Integer year,
                                                                             String printIssn,
                                                                             TestChannel testChannel)
        throws UnprocessableContentException {
        var expectedParams = new HashMap<String, String>();
        expectedParams.put("query", printIssn);
        if (nonNull(year)) {
            expectedParams.put("year", String.valueOf(year));
        }

        var expectedHits = List.of(testChannel.asJournalDto(SELF_URI_BASE, String.valueOf(year)));

        return PaginatedSearchResult.create(constructPublicationChannelUri(JOURNAL_PATH, expectedParams),
                                            DEFAULT_OFFSET_INT,
                                            DEFAULT_SIZE_INT,
                                            expectedHits.size(),
                                            expectedHits);
    }

    private PaginatedSearchResult<JournalDto> getExpectedPaginatedSearchResultIssnSearch(Integer year, String printIssn)
        throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, pid).withPrintIssn(printIssn);

        mockChannelRegistryResponse(String.valueOf(year),
                                    printIssn,
                                    List.of(testChannel.asChannelRegistryJournalBody()));

        return getExpectedSearchResult(year, printIssn, testChannel);
    }

    private PaginatedSearchResult<JournalDto> getExpectedPaginatedSearchResultIssnSearchThirdPartyDoesNotProvideYear(
        Integer year,
        String printIssn) throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(null, pid).withPrintIssn(printIssn);

        mockChannelRegistryResponse(String.valueOf(year),
                                    printIssn,
                                    List.of(testChannel.asChannelRegistryJournalBody()));

        return getExpectedSearchResult(year, printIssn, testChannel);
    }

    private void mockChannelRegistryResponse(String year, String printIssn, List<String> channelRegistryEntityResult) {
        var responseBody = getChannelRegistrySearchResponseBody(channelRegistryEntityResult, 0, 10);
        stubChannelRegistrySearchResponse(responseBody,
                                          HttpURLConnection.HTTP_OK,
                                          ISSN_QUERY_PARAM,
                                          printIssn,
                                          YEAR_QUERY_PARAM,
                                          year,
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
                                          DEFAULT_SIZE,
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM,
                                          DEFAULT_OFFSET);
    }
}
