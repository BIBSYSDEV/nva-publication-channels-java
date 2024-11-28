package no.sikt.nva.pubchannels.handler.search.publisher;

import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CHANNEL_REGISTRY_PAGE_COUNT_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.CHANNEL_REGISTRY_PAGE_NO_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_OFFSET;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_OFFSET_INT;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_SIZE;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_SIZE_INT;
import static no.sikt.nva.pubchannels.TestConstants.NAME_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.PUBLISHER_PATH;
import static no.sikt.nva.pubchannels.TestConstants.YEAR_QUERY_PARAM;
import static no.sikt.nva.pubchannels.handler.TestUtils.areEqualURIs;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchPublisherResult;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchResponseBody;
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
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandlerTest;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import no.unit.nva.stubs.FakeContext;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.UnprocessableContentException;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SearchPublisherByQueryHandlerTest extends SearchByQueryHandlerTest {

    private static final String SELF_URI_BASE = UriWrapper.fromHost(API_DOMAIN)
                                                          .addChild(CUSTOM_DOMAIN_BASE_PATH)
                                                          .addChild(PUBLISHER_PATH)
                                                          .getUri()
                                                          .toString();
    private static final Context context = new FakeContext();
    private static final TypeReference<PaginatedSearchResult<PublisherDto>> TYPE_REF = new TypeReference<>() {
    };

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new SearchPublisherByQueryHandler(environment, publicationChannelClient);
        this.customChannelPath = ChannelType.PUBLISHER.pathElement;
    }

    @ParameterizedTest(name = "Should return requested media type \"{0}\"")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType)
        throws UnprocessableContentException, IOException {
        var year = randomYear();
        var issn = randomIssn();
        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedSearchResult = getExpectedPaginatedSearchResultIssnSearch(year, issn);

        var input = constructRequest(Map.of("year", year, "query", issn), mediaType);

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
        var expectedSearchResult = getExpectedSearchResultIssnSearchThirdPartyDoesNotProvideYear(year, issn);

        var input = constructRequest(Map.of("year", year, "query", issn), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsName() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var name = randomString();
        int maxNr = 30;
        int offset = 0;
        int size = 10;
        var result = getChannelRegistrySearchPublisherResult(year, name, maxNr);
        var responseBody = getChannelRegistrySearchResponseBody(result, offset, size);
        stubChannelRegistrySearchResponse(
            responseBody, HttpURLConnection.HTTP_OK,
            YEAR_QUERY_PARAM, year,
            CHANNEL_REGISTRY_PAGE_COUNT_PARAM, DEFAULT_SIZE,
            CHANNEL_REGISTRY_PAGE_NO_PARAM, DEFAULT_OFFSET,
            NAME_QUERY_PARAM, name);
        var input = constructRequest(Map.of("year", year, "query", name), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(),
                                                       TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(result.size())));
        var expectedSearchResult = getExpectedPaginatedSearchPublisherResultNameSearch(
            result, year, name, offset, size);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryOmitsYear() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var name = randomString();
        int maxNr = 30;
        int offset = 0;
        int size = 10;
        var result = getChannelRegistrySearchPublisherResult(year, name, maxNr);
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
        var expectedSearchResult = getExpectedPaginatedSearchPublisherResultNameSearch(result, null,
                                                                                       name,
                                                                                       offset,
                                                                                       size);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsNameAndOffsetIs10() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var name = randomString();
        int offset = 10;
        int size = 10;
        int maxNr = 30;
        var result = getChannelRegistrySearchPublisherResult(year, name, maxNr);
        stubChannelRegistrySearchResponse(
            getChannelRegistrySearchResponseBody(result, offset, size),
            HttpURLConnection.HTTP_OK,
            YEAR_QUERY_PARAM, year,
            CHANNEL_REGISTRY_PAGE_COUNT_PARAM, String.valueOf(size),
            CHANNEL_REGISTRY_PAGE_NO_PARAM, String.valueOf(offset / size),
            NAME_QUERY_PARAM, name);
        var input = constructRequest(
            Map.of("year", year, "query", name,
                   "offset", String.valueOf(offset), "size", String.valueOf(size)), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(),
                                                       TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(result.size())));
        var expectedSearchResult = getExpectedPaginatedSearchPublisherResultNameSearch(
            result, year, name, offset, size);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithQueryAsId() throws IOException, UnprocessableContentException {
        var year = randomYear();
        var name = randomString();
        int offset = 10;
        int size = 10;
        int maxNr = 30;
        var result = getChannelRegistrySearchPublisherResult(year, name, maxNr);
        stubChannelRegistrySearchResponse(
            getChannelRegistrySearchResponseBody(result, offset, size),
            HttpURLConnection.HTTP_OK,
            YEAR_QUERY_PARAM,
            year,
            CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
            String.valueOf(size),
            CHANNEL_REGISTRY_PAGE_NO_PARAM,
            String.valueOf(offset / size),
            NAME_QUERY_PARAM,
            name);
        var input =
            constructRequest(
                Map.of(
                    "year",
                    year,
                    "query",
                    name,
                    "offset",
                    String.valueOf(offset),
                    "size",
                    String.valueOf(size)),
                MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        var expectedSearchResult =
            getExpectedPaginatedSearchPublisherResultNameSearch(
                result, year, name, offset, size);
        var expectedUri = expectedSearchResult.getId();
        var actualUri = pagesSearchResult.getId();

        assertTrue(areEqualURIs(actualUri, expectedUri));
    }

    private static PaginatedSearchResult<PublisherDto> getExpectedPaginatedSearchPublisherResultNameSearch(
        List<String> results,
        String year,
        String name,
        int queryOffset,
        int querySize)
        throws UnprocessableContentException {
        var expectedHits = mapToPublisherResults(results, year);
        var expectedParams = new HashMap<String, String>();
        expectedParams.put("query", name);
        if (year != null) {
            expectedParams.put("year", year);
        }

        return PaginatedSearchResult.create(constructPublicationChannelUri(PUBLISHER_PATH, null),
                                            queryOffset,
                                            querySize,
                                            expectedHits.size(),
                                            expectedHits.stream().skip(queryOffset).limit(querySize).toList(),
                                            expectedParams);
    }

    private static List<PublisherDto> mapToPublisherResults(List<String> results, String requestedYear) {
        return results.stream()
                   .map(result -> attempt(() -> objectMapper.readValue(result,
                                                                       ChannelRegistryPublisher.class)).orElseThrow())
                   .map(publisher -> toPublisherResult(publisher, requestedYear))
                   .toList();
    }

    private static PublisherDto toPublisherResult(ThirdPartyPublisher publisher, String requestedYear) {
        return PublisherDto.create(constructPublicationChannelUri(PUBLISHER_PATH, null), publisher, requestedYear);
    }

    private PaginatedSearchResult<PublisherDto> getExpectedPaginatedSearchResultIssnSearch(String year,
                                                                                           String printIssn)
        throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, pid, PublisherDto.TYPE).withPrintIssn(printIssn);

        var result = List.of(testChannel.asChannelRegistryPublisherBody());
        mockChannelRegistryResponse(String.valueOf(year), printIssn, result);
        var expectedParams = new HashMap<String, String>();
        expectedParams.put("query", printIssn);
        expectedParams.put("year", String.valueOf(year));

        return getSingleHit(testChannel.asPublisherDto(SELF_URI_BASE, year), expectedParams);
    }

    private PaginatedSearchResult<PublisherDto> getExpectedSearchResultIssnSearchThirdPartyDoesNotProvideYear(
        String year,
        String printIssn)
        throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(null, pid, PublisherDto.TYPE).withPrintIssn(printIssn);

        var result = List.of(testChannel.asChannelRegistryPublisherBody());
        mockChannelRegistryResponse(String.valueOf(year), printIssn, result);
        var expectedParams = new HashMap<String, String>();
        expectedParams.put("query", printIssn);
        expectedParams.put("year", String.valueOf(year));

        return getSingleHit(testChannel.asPublisherDto(SELF_URI_BASE, String.valueOf(year)), expectedParams);
    }

    private PaginatedSearchResult<PublisherDto> getSingleHit(PublisherDto publisherDto,
                                                             Map<String, String> queryParameters)
        throws UnprocessableContentException {
        var expectedHits = List.of(publisherDto);
        return PaginatedSearchResult.create(constructPublicationChannelUri(PUBLISHER_PATH, queryParameters),
                                            DEFAULT_OFFSET_INT,
                                            DEFAULT_SIZE_INT,
                                            expectedHits.size(),
                                            expectedHits);
    }
}
