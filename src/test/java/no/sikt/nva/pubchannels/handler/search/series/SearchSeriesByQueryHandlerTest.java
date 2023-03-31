package no.sikt.nva.pubchannels.handler.search.series;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.search.DataPortenEntityPageInformation;
import no.sikt.nva.pubchannels.dataporten.search.DataPortenLevel;
import no.sikt.nva.pubchannels.dataporten.search.DataportenEntityResult;
import no.sikt.nva.pubchannels.dataporten.search.DataportenEntityResultSet;
import no.sikt.nva.pubchannels.handler.DataportenBodyBuilder;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.UnprocessableContentException;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.sikt.nva.pubchannels.TestCommons.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestCommons.DATAPORTEN_PAGE_COUNT_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.DATAPORTEN_PAGE_NO_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.DEFAULT_OFFSET;
import static no.sikt.nva.pubchannels.TestCommons.DEFAULT_SIZE;
import static no.sikt.nva.pubchannels.TestCommons.ISSN_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.LOCALHOST;
import static no.sikt.nva.pubchannels.TestCommons.MAX_LEVEL;
import static no.sikt.nva.pubchannels.TestCommons.MIN_LEVEL;
import static no.sikt.nva.pubchannels.TestCommons.NAME_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.PID_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestCommons.YEAR_QUERY_PARAM;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;

@WireMockTest(httpsEnabled = true)
class SearchSeriesByQueryHandlerTest {

    private static final String PATH_ELEMENT = "series";
    private static final Context context = new FakeContext();
    private static final TypeReference<PaginatedSearchResult<SeriesResult>> TYPE_REF = new TypeReference<>() {
    };
    private SearchSeriesByQueryHandler handlerUnderTest;
    private ByteArrayOutputStream output;

    private static InputStream constructRequest(Map<String, String> queryParameters) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper).withQueryParameters(queryParameters).build();
    }

    private static Stream<String> invalidYearsProvider() {
        String yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", yearAfterNextYear, "21000");
    }

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {

        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");
        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new DataportenPublicationChannelClient(httpClient, dataportenBaseUri, null);

        this.handlerUnderTest = new SearchSeriesByQueryHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsIssn() throws IOException, UnprocessableContentException {
        var year = randomValidYear();
        var issn = randomIssn();
        var expectedSearchResult = getExpectedPaginatedSearchResultIssnSearch(year, issn);

        var input = constructRequest(Map.of("year", year, "query", issn));

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsPid() throws IOException, UnprocessableContentException {
        var year = randomValidYear();
        var pid = UUID.randomUUID().toString();
        var expectedSearchResult = getExpectedPaginatedSearchResultPidSearch(year, pid);

        var input = constructRequest(Map.of("year", year, "query", pid));

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsName() throws IOException, UnprocessableContentException {
        var year = randomValidYear();
        var name = randomString();
        int maxNr = 30;
        int offset = 0;
        int size = 10;
        var dataportenSearchResult = getDataportenSearchResult(year, name, maxNr);
        var responseBody = getDataportenResponseBody(dataportenSearchResult, offset, size);
        stubDataportenSearchResponse(
                responseBody, HttpURLConnection.HTTP_OK,
                YEAR_QUERY_PARAM, year,
                DATAPORTEN_PAGE_COUNT_PARAM, DEFAULT_SIZE,
                DATAPORTEN_PAGE_NO_PARAM, DEFAULT_OFFSET,
                NAME_QUERY_PARAM, name);
        var input = constructRequest(Map.of("year", year, "query", name));

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(dataportenSearchResult.size())));
        var expectedSearchresult = getExpectedPaginatedSearchResultNameSearch(
                dataportenSearchResult, year, name, offset, size);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchresult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsNameAndOffsetIs10() throws IOException, UnprocessableContentException {
        var year = randomValidYear();
        var name = randomString();
        int offset = 10;
        int size = 10;
        int maxNr = 30;
        var dataportenSearchResult = getDataportenSearchResult(year, name, maxNr);
        stubDataportenSearchResponse(
                getDataportenResponseBody(dataportenSearchResult, offset, size),
                HttpURLConnection.HTTP_OK,
                YEAR_QUERY_PARAM, year,
                DATAPORTEN_PAGE_COUNT_PARAM, String.valueOf(size),
                DATAPORTEN_PAGE_NO_PARAM, String.valueOf(offset / size),
                NAME_QUERY_PARAM, name);
        var input = constructRequest(
                Map.of("year", year, "query", name,
                        "offset", String.valueOf(offset), "size", String.valueOf(size)));

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(),
                TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(dataportenSearchResult.size())));
        var expectedSearchresult = getExpectedPaginatedSearchResultNameSearch(
                dataportenSearchResult, year, name, offset, size);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchresult.getHits().toArray()));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("invalidYearsProvider")
    void shouldReturnBadRequestWhenYearIsInvalid(String year) throws IOException {
        var queryParameters = Map.of("year", year, "query", "asd");
        var input = constructRequest(queryParameters);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Year")));
    }

    @Test
    void shouldReturnBadRequestWhenMissingQueryParamYear() throws IOException {
        var input = constructRequest(Map.of("query", randomString()));

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("year")));
    }

    @Test
    void shouldReturnBadRequestWhenMissingQuery() throws IOException {
        var input = constructRequest(Map.of("year", randomValidYear()));

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("query")));
    }

    @Test
    void shouldReturnBadRequestWhenQueryParamTooLong() throws IOException {
        var input = constructRequest(Map.of("year", randomValidYear(), "query", "Lorem Ipsum "
                + "is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the "
                + "industry's standard dummy text ever since the 1500s, when an unknown printer took a galley "
                + "of type and scrambled it to make a type specimen book. It has survived not only five centuries,"
                + " but also the l"));

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Query")));
    }

    @Test
    void shouldReturnBadRequestWhenOffsetAndSizeAreNotDivisible() throws IOException {
        var input = constructRequest(Map.of("year", randomValidYear(), "query", randomString(),
                "offset", "5", "size", "8"));

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Offset")));

    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCode() throws IOException {

        var year = randomValidYear();
        var name = randomString();
        int maxNr = 30;
        var dataportenSearchResult = getDataportenSearchResult(year, name, maxNr);
        var responseBody = getDataportenResponseBody(dataportenSearchResult, 0, 10);
        stubDataportenSearchResponse(
                responseBody, HttpURLConnection.HTTP_INTERNAL_ERROR,
                YEAR_QUERY_PARAM, year,
                DATAPORTEN_PAGE_COUNT_PARAM, DEFAULT_SIZE,
                DATAPORTEN_PAGE_NO_PARAM, DEFAULT_OFFSET,
                NAME_QUERY_PARAM, name);
        var input = constructRequest(Map.of("year", year, "query", name));

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(equalTo("Unexpected response from upstream!")));
    }

    private PaginatedSearchResult<SeriesResult> getExpectedPaginatedSearchResultIssnSearch(
            String year,
            String printIssn)
            throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var name = randomString();
        var electronicIssn = randomIssn();
        var level = randomLevel();
        var landingPage = randomUri();

        var dataportenEntityResult = List.of(
                createDataportenResult(year, printIssn, pid, name, electronicIssn, landingPage, level)
        );
        var responseBody = getDataportenResponseBody(dataportenEntityResult, 0, 10);
        stubDataportenSearchResponse(responseBody, HttpURLConnection.HTTP_OK,
                ISSN_QUERY_PARAM, printIssn,
                YEAR_QUERY_PARAM, year,
                DATAPORTEN_PAGE_COUNT_PARAM, DEFAULT_SIZE,
                DATAPORTEN_PAGE_NO_PARAM, DEFAULT_OFFSET
        );

        return getSingleHit(year, printIssn, pid, name, electronicIssn, level, landingPage);
    }

    private PaginatedSearchResult<SeriesResult> getExpectedPaginatedSearchResultPidSearch(String year, String pid)
            throws UnprocessableContentException {
        var printIssn = randomIssn();
        var name = randomString();
        var electronicIssn = randomIssn();
        var level = randomLevel();
        var landingPage = randomUri();
        var dataportenResult = List.of(
                createDataportenResult(year, printIssn, pid, name, electronicIssn, landingPage, level)
        );
        var responseBody = getDataportenResponseBody(dataportenResult, 0, 10);
        stubDataportenSearchResponse(responseBody, HttpURLConnection.HTTP_OK,
                YEAR_QUERY_PARAM, year,
                DATAPORTEN_PAGE_COUNT_PARAM, DEFAULT_SIZE,
                DATAPORTEN_PAGE_NO_PARAM, DEFAULT_OFFSET,
                PID_QUERY_PARAM, pid);

        return getSingleHit(year, printIssn, pid, name, electronicIssn, level, landingPage);
    }

    private PaginatedSearchResult<SeriesResult> getSingleHit(
            String year,
            String printIssn,
            String pid,
            String name,
            String electronicIssn,
            String level,
            URI landingPage) throws UnprocessableContentException {

        var expectedHits = List.of(
                SeriesResult.create(
                        constructPublicationChannelUri(null),
                        new DataportenEntityResult(
                                pid,
                                name,
                                printIssn,
                                electronicIssn,
                                new DataPortenLevel(Integer.parseInt(year), level),
                                landingPage)
                ));

        return PaginatedSearchResult.create(
                constructPublicationChannelUri(
                        Map.of("year", year, "query", printIssn, "offset", DEFAULT_OFFSET, "size", DEFAULT_SIZE)),
                0,
                expectedHits.size(),
                expectedHits.size(),
                expectedHits);
    }

    private String getDataportenResponseBody(List<DataportenEntityResult> results, int offset, int size) {

        return new DataportenBodyBuilder()
                .withEntityPageInformation(new DataPortenEntityPageInformation(results.size()))
                .withEntityResultSet(new DataportenEntityResultSet(
                        results.stream().skip(offset).limit(size).collect(Collectors.toList())))
                .build();
    }

    private DataportenEntityResult createDataportenResult(
            String year,
            String issn,
            String pid,
            String name,
            String electronicIssn,
            URI landingPage,
            String level) {
        return new DataportenEntityResult(pid,
                name,
                issn,
                electronicIssn,
                new DataPortenLevel(Integer.parseInt(year), level), landingPage);
    }

    private String randomLevel() {
        return String.valueOf((int) Math.floor(Math.random() * (MAX_LEVEL - MIN_LEVEL + 1) + MIN_LEVEL));
    }

    private void stubDataportenSearchResponse(String body, int status, String... queryValue) {
        if (queryValue.length % 2 != 0) {
            throw new RuntimeException();
        }
        var queryParams = getStringStringValuePatternHashMap(queryValue);
        var url = getDataPortenRequestUrl(queryValue);

        stubFor(get(url.toString()).withHeader("Accept", WireMock.equalTo("application/json"))
                .withQueryParams(queryParams)
                .willReturn(aResponse().withStatus(status)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(body)
                )
        );
    }

    private Map<String, StringValuePattern> getStringStringValuePatternHashMap(String... queryValue) {
        var queryParams = new HashMap<String, StringValuePattern>();
        for (int i = 0; i < queryValue.length; i = i + 2) {
            queryParams.put(queryValue[i], WireMock.equalTo(queryValue[i + 1]));
        }
        return queryParams;
    }

    private StringBuilder getDataPortenRequestUrl(String... queryValue) {
        var url = new StringBuilder("/findseries/channels");
        for (int i = 0; i < queryValue.length; i = i + 2) {
            url.append(i == 0 ? "?" : "&");
            url.append(queryValue[i]).append("=").append(queryValue[i + 1]);
        }
        return url;
    }

    private String randomValidYear() {
        var bound = (LocalDate.now().getYear() + 1) - 1900;
        return Integer.toString(1900 + randomInteger(bound));
    }

    private URI constructPublicationChannelUri(Map<String, String> queryParams, String... pathElements) {
        var uri = new UriWrapper(HTTPS, LOCALHOST)
                .addChild(CUSTOM_DOMAIN_BASE_PATH, PATH_ELEMENT)
                .addChild(pathElements)
                .getUri();
        if (Objects.nonNull(queryParams)) {
            return UriWrapper.fromUri(uri).addQueryParameters(queryParams).getUri();
        }
        return uri;
    }

    private List<DataportenEntityResult> getDataportenSearchResult(String year, String name, int maxNr) {
        return IntStream.range(0, maxNr)
                .mapToObj(i ->
                        new DataportenEntityResult(
                                UUID.randomUUID().toString(),
                                name + randomString(),
                                null,
                                randomIssn(),
                                new DataPortenLevel(Integer.parseInt(year), randomLevel()),
                                randomUri()))
                .collect(Collectors.toList());
    }

    private PaginatedSearchResult<SeriesResult> getExpectedPaginatedSearchResultNameSearch(
            List<DataportenEntityResult> dataportenResults,
            String year,
            String name, int queryOffset, int querySize) throws UnprocessableContentException {
        var expectedHits = dataportenResults
                .stream()
                .map(this::toResult)
                .collect(Collectors.toList());

        return PaginatedSearchResult.create(
                constructPublicationChannelUri(Map.of("year", year, "query", name)),
                queryOffset,
                querySize,
                expectedHits.size(),
                expectedHits.stream().skip(queryOffset).limit(querySize).collect(Collectors.toList()),
                Map.of("year", year, "query", name,
                        "offset", String.valueOf(queryOffset), "size", String.valueOf(querySize)));
    }

    private SeriesResult toResult(DataportenEntityResult entityResult) {
        return SeriesResult.create(constructPublicationChannelUri(null), entityResult);
    }
}
