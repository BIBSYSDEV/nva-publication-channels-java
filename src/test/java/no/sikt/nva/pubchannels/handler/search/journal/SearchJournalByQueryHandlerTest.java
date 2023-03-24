package no.sikt.nva.pubchannels.handler.search.journal;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.dataporten.search.DataPortenEntityPageInformation;
import no.sikt.nva.pubchannels.dataporten.search.DataPortenLevel;
import no.sikt.nva.pubchannels.dataporten.search.DataportenEntityResultSet;
import no.sikt.nva.pubchannels.dataporten.search.DataportenJournalResult;
import no.sikt.nva.pubchannels.handler.DataportenBodyBuilder;
import no.sikt.nva.pubchannels.handler.search.PagedSearchResult;
import no.sikt.nva.pubchannels.model.Contexts;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
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
class SearchJournalByQueryHandlerTest {

    public static final String YEAR_QUERY_PARAM = "year";
    public static final String ISSN_QUERY_PARAM = "issn";
    public static final String CUSTOM_DOMAIN_BASE_PATH = "publication-channels";
    public static final String JOURNAL_PATH_ELEMENT = "journal";
    public static final String LOCALHOST = "localhost";
    public static final int MAX_LEVEL = 2;
    public static final double MIN_LEVEL = 0;
    public static final String DEFAULT_OFFSET = "0";
    public static final String DEFAULT_SIZE = "10";
    private static final Context context = new FakeContext();
    private SearchJournalByQueryHandler handlerUnderTest;
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

        this.handlerUnderTest = new SearchJournalByQueryHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsIssn() throws IOException {
        var year = randomValidYear();
        var issn = randomIssn();
        var expectedSearchResult = getExpectedPagedSearchResult(year, issn);

        var input = constructRequest(Map.of("year", year, "query", issn));

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PagedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(),
                new TypeReference<PagedSearchResult<JournalResult>>() {});

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("invalidYearsProvider")
    void shouldReturnBadRequestWhenYearIsInvalid(String year) throws IOException {
        var queryParameters = Map.of("year", year);
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

    private PagedSearchResult<JournalResult> getExpectedPagedSearchResult(String year, String issn) {
        var identifier = UUID.randomUUID().toString();
        var name = randomString();
        var electronicIssn = randomIssn();
        var landingPage = randomUri();
        var level = randomLevel();

        var expectedHits = List.of(new JournalResult(constructPublicationChannelUri(null, identifier, year),
                name,
                electronicIssn,
                issn,
                new ScientificValueMapper().map(level),
                landingPage));

        var expectedSearchResultResponse = new PagedSearchResult<>(
                URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT),
                constructPublicationChannelUri(
                        Map.of("year", year, "query", issn, "offset", DEFAULT_OFFSET, "size", DEFAULT_SIZE)),
                expectedHits.size(),
                null,
                null,
                expectedHits);

        mockDataportenIssnSearch(year, issn, identifier, name, electronicIssn, landingPage, level);
        return expectedSearchResultResponse;
    }

    private void mockDataportenIssnSearch(String year,
                                          String issn,
                                          String identifier,
                                          String name,
                                          String electronicIssn,
                                          URI landingPage,
                                          String level) {
        var body = getDataportenResponseBody(year, issn, identifier, name, electronicIssn, landingPage, level);
        stubDataportenSearchResponse(body, ISSN_QUERY_PARAM, issn, YEAR_QUERY_PARAM, year);
    }

    private String getDataportenResponseBody(String year,
                                             String issn,
                                             String identifier,
                                             String name,
                                             String electronicIssn,
                                             URI landingPage,
                                             String level) {

        List<DataportenJournalResult> results = List.of(new DataportenJournalResult(identifier,
                name,
                issn,
                electronicIssn,
                new DataPortenLevel(Integer.parseInt(year), level), landingPage)
        );
        return new DataportenBodyBuilder()
                .withEntityPageInformation(new DataPortenEntityPageInformation(results.size()))
                .withEntityResultSet(new DataportenEntityResultSet(results))
                .build();
    }

    private String randomLevel() {
        return String.valueOf((int) Math.floor(Math.random() * (MAX_LEVEL - MIN_LEVEL + 1) + MIN_LEVEL));
    }

    private void stubDataportenSearchResponse(String body, String... queryValue) {
        if (queryValue.length % 2 != 0) {
            throw new RuntimeException();
        }
        var queryParams = getStringStringValuePatternHashMap(queryValue);
        var url = getDataPortenRequestUrl(queryValue);

        stubFor(get(url.toString()).withHeader("Accept", WireMock.equalTo("application/json"))
                .withQueryParams(queryParams)
                .willReturn(aResponse().withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(body)
                )
        );
    }

    private HashMap<String, StringValuePattern> getStringStringValuePatternHashMap(String... queryValue) {
        var queryParams = new HashMap<String, StringValuePattern>();
        for (int i = 0; i < queryValue.length; i = i + 2) {
            queryParams.put(queryValue[i], WireMock.equalTo(queryValue[i + 1]));
        }
        return queryParams;
    }

    private StringBuilder getDataPortenRequestUrl(String... queryValue) {
        var url = new StringBuilder("/findjournal/channels");
        for (int i = 0; i < queryValue.length; i = i + 2) {
            if (i == 0) {
                url.append("?");
            } else {
                url.append("&");
            }
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
                .addChild(CUSTOM_DOMAIN_BASE_PATH, JOURNAL_PATH_ELEMENT)
                .addChild(pathElements)
                .getUri();
        if (Objects.nonNull(queryParams)) {
            return UriWrapper.fromUri(uri).addQueryParameters(queryParams).getUri();
        }
        return uri;
    }

}
