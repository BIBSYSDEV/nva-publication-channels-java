package no.sikt.nva.pubchannels.handler.search;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
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
import static no.sikt.nva.pubchannels.TestConstants.NAME_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.TOO_LONG_INPUT_STRING;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.TestConstants.YEAR_QUERY_PARAM;
import static no.sikt.nva.pubchannels.handler.TestUtils.areEqualURIs;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchResponseBody;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchResult;
import static no.sikt.nva.pubchannels.handler.TestUtils.getStringStringValuePatternHashMap;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
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
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.UnprocessableContentException;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
public abstract class SearchByQueryHandlerTest {

    protected static final Context context = new FakeContext();
    protected static Environment environment;
    protected static String year = randomYear();
    protected static String issn = randomIssn();
    protected static String name = randomString();
    protected static String pid = UUID.randomUUID().toString();
    protected final ByteArrayOutputStream output = new ByteArrayOutputStream();
    protected SearchByQueryHandler<?> handlerUnderTest;
    protected String customChannelPath;
    protected ChannelRegistryClient publicationChannelClient;
    protected String type;
    protected URI selfBaseUri;
    protected TypeReference<PaginatedSearchResult<?>> typeRef;

    protected abstract PaginatedSearchResult<?> getExpectedSearchResult(String year,
                                                                        String queryParamValue,
                                                                        TestChannel testChannel)
        throws UnprocessableContentException;

    protected abstract PaginatedSearchResult<?> getActualSearchResult(GatewayResponse<?> response)
        throws JsonProcessingException;

    protected void stubChannelRegistrySearchResponse(String body, int status, String... queryValue) {
        if (queryValue.length % 2 != 0) {
            throw new RuntimeException();
        }
        var queryParams = getStringStringValuePatternHashMap(queryValue);

        var testUrl = "/" + customChannelPath + "/channels";
        stubFor(get(urlPathEqualTo(testUrl)).withHeader("Accept", WireMock.equalTo("application/json"))
                                            .withQueryParams(queryParams)
                                            .willReturn(aResponse().withStatus(status)
                                                                   .withHeader("Content-Type",
                                                                               "application/json;charset=UTF-8")
                                                                   .withBody(body)));
    }

    protected void mockChannelRegistryResponse(String year,
                                               String queryParamKey,
                                               String queryParamValue,
                                               List<String> channelRegistryEntityResult) {
        mockChannelRegistryResponse(year,
                                    queryParamKey,
                                    queryParamValue,
                                    channelRegistryEntityResult,
                                    DEFAULT_SIZE_INT,
                                    DEFAULT_OFFSET_INT);
    }

    protected void mockChannelRegistryResponse(String year,
                                               String queryParamKey,
                                               String queryParamValue,
                                               List<String> channelRegistryEntityResult,
                                               int size,
                                               int offset) {

        var responseBody = getChannelRegistrySearchResponseBody(channelRegistryEntityResult, offset, size);
        var pageNumber = size == 0 ? "0" : String.valueOf(offset / size);
        stubChannelRegistrySearchResponse(responseBody,
                                          HttpURLConnection.HTTP_OK,
                                          YEAR_QUERY_PARAM,
                                          year,
                                          queryParamKey,
                                          queryParamValue,
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
                                          String.valueOf(size),
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM,
                                          pageNumber);
    }

    @BeforeAll
    static void commonBeforeAll() {
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);
    }

    @BeforeEach
    void commonBeforeEach(WireMockRuntimeInfo runtimeInfo) {
        var channelRegistryBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        publicationChannelClient = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);
    }

    @AfterEach
    void tearDown() throws IOException {
        output.flush();
    }

    @ParameterizedTest(name = "Should return requested media type \"{0}\"")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType)
        throws IOException, UnprocessableContentException {
        var testChannel = new TestChannel(year, pid, type).withPrintIssn(issn);
        mockChannelRegistryResponse(year, ISSN_QUERY_PARAM, issn, List.of(testChannel.asChannelRegistryResponseBody()));

        var expectedSearchResult = getExpectedSearchResult(year, issn, testChannel);
        var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();

        var input = constructRequest(Map.of("year", year, "query", issn), mediaType);
        this.handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualSearchResult = getActualSearchResult(response);
        var contentType = response.getHeaders().get(CONTENT_TYPE);

        assertThat(actualSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
        assertThat(contentType, is(equalTo(expectedMediaType)));
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsIssn() throws IOException, UnprocessableContentException {
        var testChannel = new TestChannel(year, pid, type).withPrintIssn(issn);
        mockChannelRegistryResponse(year, ISSN_QUERY_PARAM, issn, List.of(testChannel.asChannelRegistryResponseBody()));

        var expectedSearchResult = getExpectedSearchResult(year, issn, testChannel);

        var input = constructRequest(Map.of("year", year, "query", issn), MediaType.ANY_TYPE);
        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualSearchResult = getActualSearchResult(response);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(actualSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithRequestedYearIfThirdPartyDoesNotProvideYear()
        throws IOException, UnprocessableContentException {
        var testChannel = new TestChannel(null, pid, type).withPrintIssn(issn);
        mockChannelRegistryResponse(year, ISSN_QUERY_PARAM, issn, List.of(testChannel.asChannelRegistryResponseBody()));
        var expectedSearchResult = getExpectedSearchResult(year, issn, testChannel);

        var input = constructRequest(Map.of("year", year, "query", issn), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualSearchResult = getActualSearchResult(response);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(actualSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsName() throws IOException, UnprocessableContentException {
        var testChannel = new TestChannel(year, pid, type).withName(name);
        mockChannelRegistryResponse(year, NAME_QUERY_PARAM, name, List.of(testChannel.asChannelRegistryResponseBody()));

        var expectedSearchResult = getExpectedSearchResult(year, name, testChannel);

        var input = constructRequest(Map.of("year", year, "query", name), MediaType.ANY_TYPE);
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualSearchResult = getActualSearchResult(response);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(actualSearchResult.getTotalHits(), is(equalTo(expectedSearchResult.getHits().size())));
        assertThat(actualSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithIdMatchingRequest() throws IOException {
        var size = 20;
        var offset = 40;
        var testChannel = new TestChannel(year, pid, type).withPrintIssn(issn);
        var expectedId = UriWrapper.fromUri(selfBaseUri)
                                   .addQueryParameter("year", year)
                                   .addQueryParameter("query", issn)
                                   .addQueryParameter("size", String.valueOf(size))
                                   .addQueryParameter("offset", String.valueOf(offset))
                                   .getUri();
        mockChannelRegistryResponse(year,
                                    ISSN_QUERY_PARAM,
                                    testChannel.getPrintIssnValue(),
                                    List.of(testChannel.asChannelRegistryResponseBody()),
                                    size,
                                    offset);

        var input = constructRequest(Map.of("year",
                                            year,
                                            "query",
                                            issn,
                                            "size",
                                            String.valueOf(size),
                                            "offset",
                                            String.valueOf(offset)), MediaType.ANY_TYPE);
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualSearchResult = getActualSearchResult(response);
        var actualId = actualSearchResult.getId();

        assertTrue(areEqualURIs(actualId, expectedId));
    }

    @ParameterizedTest(name = "Should accept offset \"{0}\"")
    @ValueSource(ints = {0, 10, 20})
    void shouldReturnIdWithOffsetMatchingRequest(int offset) throws IOException {
        var testChannel = new TestChannel(year, pid, type).withPrintIssn(issn);
        mockChannelRegistryResponse(year,
                                    ISSN_QUERY_PARAM,
                                    testChannel.getPrintIssnValue(),
                                    List.of(testChannel.asChannelRegistryResponseBody()),
                                    DEFAULT_SIZE_INT,
                                    offset);

        var input = constructRequest(Map.of("year", year, "query", issn, "offset", String.valueOf(offset)),
                                     MediaType.ANY_TYPE);
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualSearchResult = getActualSearchResult(response);

        assertThat(actualSearchResult.getId().toString(), containsStringIgnoringCase("offset=" + offset));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#invalidYearsProvider")
    void shouldReturnBadRequestWhenYearIsInvalid(String year) throws IOException {
        var queryParameters = Map.of("year", year, "query", "asd");
        var input = constructRequest(queryParameters, MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        var problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(problem.getDetail(), is(containsString("Year")));
    }

    @Test
    void shouldReturnBadRequestWhenMissingQuery() throws IOException {
        var input = constructRequest(Map.of("year", randomYear()), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        var problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(problem.getDetail(), is(containsString("query")));
    }

    @Test
    void shouldReturnBadRequestWhenQueryParamTooLong() throws IOException {
        var input = constructRequest(Map.of("year", randomYear(), "query", TOO_LONG_INPUT_STRING), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        var problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(problem.getDetail(), is(containsString("Query")));
    }

    @Test
    void shouldReturnBadRequestWhenOffsetAndSizeAreNotDivisible() throws IOException {
        var input = constructRequest(Map.of("year", randomYear(), "query", randomString(), "offset", "5", "size", "8"),
                                     MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        var problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(problem.getDetail(), is(containsString("Offset")));
    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCode() throws IOException {

        int maxNr = 30;
        var result = getChannelRegistrySearchResult(year, name, maxNr);
        var responseBody = getChannelRegistrySearchResponseBody(result, 0, 10);
        stubChannelRegistrySearchResponse(responseBody,
                                          HttpURLConnection.HTTP_INTERNAL_ERROR,
                                          YEAR_QUERY_PARAM,
                                          year,
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
                                          DEFAULT_SIZE,
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM,
                                          DEFAULT_OFFSET,
                                          NAME_QUERY_PARAM,
                                          name);
        var input = constructRequest(Map.of("year", year, "query", name), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        var problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }
}
