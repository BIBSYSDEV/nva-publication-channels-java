package no.sikt.nva.pubchannels.handler.search.series;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestCommons.CHANNEL_REGISTRY_PAGE_COUNT_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.CHANNEL_REGISTRY_PAGE_NO_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.DEFAULT_OFFSET;
import static no.sikt.nva.pubchannels.TestCommons.DEFAULT_OFFSET_INT;
import static no.sikt.nva.pubchannels.TestCommons.DEFAULT_SIZE;
import static no.sikt.nva.pubchannels.TestCommons.DEFAULT_SIZE_INT;
import static no.sikt.nva.pubchannels.TestCommons.ISSN_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.NAME_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.SERIES_PATH;
import static no.sikt.nva.pubchannels.TestCommons.YEAR_QUERY_PARAM;
import static no.sikt.nva.pubchannels.handler.TestUtils.areEqualURIs;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistryRequestUrl;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistryResponseBody;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchResult;
import static no.sikt.nva.pubchannels.handler.TestUtils.getStringStringValuePatternHashMap;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.net.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySeries;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.handler.model.SeriesDto;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.UnprocessableContentException;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
class SearchSeriesByQueryHandlerTest {

    private static final Context context = new FakeContext();
    private static final TypeReference<PaginatedSearchResult<SeriesDto>> TYPE_REF = new TypeReference<>() {
    };
    private static final String SELF_URI_BASE = "https://localhost/publication-channels/" + SERIES_PATH;
    private SearchSeriesByQueryHandler handlerUnderTest;
    private ByteArrayOutputStream output;

    public static PaginatedSearchResult<SeriesDto> getExpectedPaginatedSearchResultNameSearch(List<String> results,
                                                                                              String year,
                                                                                              String name,
                                                                                              int queryOffset,
                                                                                              int querySize)
        throws UnprocessableContentException {
        var expectedHits = mapToSeriesResults(results, year);
        var expectedParams = new HashMap<String, String>();
        expectedParams.put("query", name);
        if (year != null) {
            expectedParams.put("year", year);
        }

        return PaginatedSearchResult.create(constructPublicationChannelUri(SERIES_PATH, null),
                                            queryOffset,
                                            querySize,
                                            expectedHits.size(),
                                            expectedHits.stream()
                                                .skip(queryOffset)
                                                .limit(querySize)
                                                .collect(Collectors.toList()),
                                            expectedParams);
    }

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {

        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");
        var channelRegistryBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);

        this.handlerUnderTest = new SearchSeriesByQueryHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @ParameterizedTest
    @DisplayName("Should return requested media type")
    @MethodSource("no.sikt.nva.pubchannels.TestCommons#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType)
        throws UnprocessableContentException, IOException {
        var year = randomYear();
        var issn = randomIssn();
        final var expectedMediaType = mediaType.equals(MediaType.ANY_TYPE)
                                          ? MediaType.JSON_UTF_8.toString()
                                          : mediaType.toString();
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
        var year = String.valueOf(randomYear());
        var issn = randomIssn();
        var expectedSearchResult = getExpectedPaginatedSearchResultIssnSearchThirdPartyDoesNotProvideYear(year, issn);

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
        var yearString = String.valueOf(year);
        var name = randomString();
        int maxNr = 30;
        int offset = 0;
        int size = 10;
        var result = getChannelRegistrySearchResult(year, name, maxNr);
        var responseBody = getChannelRegistryResponseBody(result, offset, size);
        stubChannelRegistrySearchResponse(
            responseBody, HttpURLConnection.HTTP_OK,
            YEAR_QUERY_PARAM, yearString,
            CHANNEL_REGISTRY_PAGE_COUNT_PARAM, DEFAULT_SIZE,
            CHANNEL_REGISTRY_PAGE_NO_PARAM, DEFAULT_OFFSET,
            NAME_QUERY_PARAM, name);
        var input = constructRequest(Map.of("year", yearString, "query", name), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(result.size())));
        var expectedSearchResult = getExpectedPaginatedSearchResultNameSearch(
            result, yearString, name, offset, size);
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
        var responseBody = getChannelRegistryResponseBody(result, offset, size);
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
        var expectedSearchResult = getExpectedPaginatedSearchResultNameSearch(result, null, name, offset, size);
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
        var result = getChannelRegistrySearchResult(year, name, maxNr);
        stubChannelRegistrySearchResponse(
            getChannelRegistryResponseBody(result, offset, size),
            HttpURLConnection.HTTP_OK,
            YEAR_QUERY_PARAM, yearString,
            CHANNEL_REGISTRY_PAGE_COUNT_PARAM, String.valueOf(size),
            CHANNEL_REGISTRY_PAGE_NO_PARAM, String.valueOf(offset / size),
            NAME_QUERY_PARAM, name);
        var input = constructRequest(
            Map.of("year", yearString, "query", name,
                   "offset", String.valueOf(offset), "size", String.valueOf(size)), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(),
                                                       TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(result.size())));
        var expectedSearchResult = getExpectedPaginatedSearchResultNameSearch(result, yearString, name,
                                                                              offset, size);
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
        var result = getChannelRegistrySearchResult(year, name, maxNr);
        stubChannelRegistrySearchResponse(
            getChannelRegistryResponseBody(result, offset, size),
            HttpURLConnection.HTTP_OK,
            YEAR_QUERY_PARAM,
            yearString,
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
                    yearString,
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
            getExpectedPaginatedSearchResultNameSearch(result, yearString, name, offset, size);

        var expectedUri = expectedSearchResult.getId();
        var actualUri = pagesSearchResult.getId();

        assertTrue(areEqualURIs(actualUri, expectedUri));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("invalidYearsProvider")
    void shouldReturnBadRequestWhenYearIsInvalid(String year) throws IOException {
        var queryParameters = Map.of("year", year, "query", "asd");
        var input = constructRequest(queryParameters, MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Year")));
    }

    @Test
    void shouldReturnBadRequestWhenMissingQuery() throws IOException {
        var input = constructRequest(Map.of("year", String.valueOf(randomYear())), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("query")));
    }

    @Test
    void shouldReturnBadRequestWhenQueryParamTooLong() throws IOException {
        var input = constructRequest(Map.of("year", String.valueOf(randomYear()), "query", "Lorem Ipsum "
                                                                                           + "is simply dummy text of"
                                                                                           + " the "
                                                                                           + "printing and "
                                                                                           + "typesetting industry."
                                                                                           + " Lorem Ipsum has been "
                                                                                           + "the "
                                                                                           + "industry's standard "
                                                                                           + "dummy text "
                                                                                           + "ever since the 1500s, "
                                                                                           + "when an "
                                                                                           + "unknown printer took a "
                                                                                           + "galley "
                                                                                           + "of type and scrambled "
                                                                                           + "it to make a"
                                                                                           + " type specimen book. It"
                                                                                           + " has "
                                                                                           + "survived not only five "
                                                                                           + "centuries,"
                                                                                           + " but also the l"),
                                     MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Query")));
    }

    @Test
    void shouldReturnBadRequestWhenOffsetAndSizeAreNotDivisible() throws IOException {
        var input = constructRequest(Map.of("year", String.valueOf(randomYear()), "query", randomString(),
                                            "offset", "5", "size", "8"), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Offset")));
    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCode() throws IOException {

        var year = randomYear();
        var name = randomString();
        int maxNr = 30;
        var result = getChannelRegistrySearchResult(year, name, maxNr);
        var responseBody = getChannelRegistryResponseBody(result, 0, 10);
        stubChannelRegistrySearchResponse(
            responseBody, HttpURLConnection.HTTP_INTERNAL_ERROR,
            YEAR_QUERY_PARAM, String.valueOf(year),
            CHANNEL_REGISTRY_PAGE_COUNT_PARAM, DEFAULT_SIZE,
            CHANNEL_REGISTRY_PAGE_NO_PARAM, DEFAULT_OFFSET,
            NAME_QUERY_PARAM, name);
        var input = constructRequest(Map.of("year", String.valueOf(year), "query", name), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream!")));
    }

    private static InputStream constructRequest(Map<String, String> queryParameters, MediaType mediaType)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper)
                   .withHeaders(Map.of(ACCEPT, mediaType.toString()))
                   .withQueryParameters(queryParameters).build();
    }

    private static Stream<String> invalidYearsProvider() {
        String yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", yearAfterNextYear, "21000");
    }

    private static List<SeriesDto> mapToSeriesResults(List<String> results, String requestedYear) {
        return results
                   .stream()
                   .map(result -> attempt(
                       () -> objectMapper.readValue(result, ChannelRegistrySeries.class)).orElseThrow())
                   .map(series -> toResult(series, requestedYear))
                   .collect(Collectors.toList());
    }

    private static SeriesDto toResult(ThirdPartySeries series, String requestedYear) {
        return SeriesDto.create(constructPublicationChannelUri(SERIES_PATH, null), series, requestedYear);
    }

    private PaginatedSearchResult<SeriesDto> getExpectedPaginatedSearchResultIssnSearch(
        int year, String printIssn)
        throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, pid).withPrintIssn(printIssn);
        return createSearchResult(testChannel, String.valueOf(year), printIssn);
    }

    private PaginatedSearchResult<SeriesDto> getExpectedPaginatedSearchResultIssnSearchThirdPartyDoesNotProvideYear(
        String year, String printIssn)
        throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(null, pid).withPrintIssn(printIssn);
        return createSearchResult(testChannel, String.valueOf(year), printIssn);
    }

    private PaginatedSearchResult<SeriesDto> createSearchResult(TestChannel testChannel,
                                                                String year,
                                                                String printIssn)
        throws UnprocessableContentException {
        mockChannelRegistryResponse(year, printIssn, List.of(testChannel.asChannelRegistrySeriesBody()));

        var expectedHits = List.of(testChannel.asSeriesDto(SELF_URI_BASE, year));
        return PaginatedSearchResult.create(constructPublicationChannelUri(SERIES_PATH,
                                                                           Map.of("year", year, "query", printIssn)),
                                            DEFAULT_OFFSET_INT,
                                            DEFAULT_SIZE_INT,
                                            expectedHits.size(),
                                            expectedHits);
    }

    private void mockChannelRegistryResponse(String year, String printIssn, List<String> result) {
        var responseBody = getChannelRegistryResponseBody(result, 0, 10);
        stubChannelRegistrySearchResponse(responseBody, HttpURLConnection.HTTP_OK,
                                          ISSN_QUERY_PARAM, printIssn,
                                          YEAR_QUERY_PARAM, year,
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM, DEFAULT_SIZE,
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM, DEFAULT_OFFSET
        );
    }

    private void stubChannelRegistrySearchResponse(String body, int status, String... queryValue) {
        if (queryValue.length % 2 != 0) {
            throw new RuntimeException();
        }
        var queryParams = getStringStringValuePatternHashMap(queryValue);
        var url = getChannelRegistryRequestUrl("findseries", queryValue);

        stubFor(get(url.toString()).withHeader("Accept", WireMock.equalTo("application/json"))
                    .withQueryParams(queryParams)
                    .willReturn(aResponse().withStatus(status)
                                    .withHeader("Content-Type", "application/json;charset=UTF-8")
                                    .withBody(body)
                    )
        );
    }
}
