package no.sikt.nva.pubchannels.handler.search;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CHANNEL_REGISTRY_PAGE_COUNT_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.CHANNEL_REGISTRY_PAGE_NO_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_OFFSET;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_SIZE;
import static no.sikt.nva.pubchannels.TestConstants.ISSN_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.NAME_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.TOO_LONG_INPUT_STRING;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.TestConstants.YEAR_QUERY_PARAM;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistryRequestUrl;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchResponseBody;
import static no.sikt.nva.pubchannels.handler.TestUtils.getChannelRegistrySearchResult;
import static no.sikt.nva.pubchannels.handler.TestUtils.getStringStringValuePatternHashMap;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
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
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
public abstract class SearchByQueryHandlerTest {

    private static final Context context = new FakeContext();
    protected SearchByQueryHandler<?> handlerUnderTest;
    protected final ByteArrayOutputStream output = new ByteArrayOutputStream();
    protected static Environment environment;
    protected ChannelRegistryClient publicationChannelClient;

    protected abstract String getPath();

    protected void stubChannelRegistrySearchResponse(String body, int status, String... queryValue) {
        if (queryValue.length % 2 != 0) {
            throw new RuntimeException();
        }
        var queryParams = getStringStringValuePatternHashMap(queryValue);
        var url = getChannelRegistryRequestUrl(getPath(), queryValue);

        stubFor(get(url.toString()).withHeader("Accept", WireMock.equalTo("application/json"))
                                   .withQueryParams(queryParams)
                                   .willReturn(aResponse().withStatus(status)
                                                          .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                          .withBody(body)));
    }

    protected void mockChannelRegistryResponse(String year,
                                               String printIssn,
                                               List<String> channelRegistryEntityResult) {
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
        var input = constructRequest(Map.of("year", randomYear(), "query", TOO_LONG_INPUT_STRING),
                                     MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        var problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(problem.getDetail(), is(containsString("Query")));
    }

    @Test
    void shouldReturnBadRequestWhenOffsetAndSizeAreNotDivisible() throws IOException {
        var input = constructRequest(Map.of("year",
                                            randomYear(),
                                            "query",
                                            randomString(),
                                            "offset",
                                            "5",
                                            "size",
                                            "8"), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        var problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(problem.getDetail(), is(containsString("Offset")));
    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCode() throws IOException {

        var year = randomYear();
        var name = randomString();
        int maxNr = 30;
        var result = getChannelRegistrySearchResult(year, name, maxNr);
        var responseBody = getChannelRegistrySearchResponseBody(result, 0, 10);
        stubChannelRegistrySearchResponse(responseBody,
                                          HttpURLConnection.HTTP_INTERNAL_ERROR,
                                          YEAR_QUERY_PARAM,
                                          String.valueOf(year),
                                          CHANNEL_REGISTRY_PAGE_COUNT_PARAM,
                                          DEFAULT_SIZE,
                                          CHANNEL_REGISTRY_PAGE_NO_PARAM,
                                          DEFAULT_OFFSET,
                                          NAME_QUERY_PARAM,
                                          name);
        var input = constructRequest(Map.of("year", String.valueOf(year), "query", name), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);
        var problem = response.getBodyObject(Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }
}
