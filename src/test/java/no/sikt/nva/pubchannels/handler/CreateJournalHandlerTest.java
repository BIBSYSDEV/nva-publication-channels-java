package no.sikt.nva.pubchannels.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.TokenBody;
import no.sikt.nva.pubchannels.model.CreateJournalRequest;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

@WireMockTest(httpsEnabled = true)
class CreateJournalHandlerTest {

    public static final TokenBody TOKEN_BODY = new TokenBody("token1", "Bearer");
    public static final String LOCATION = "https://localhost/findjournal";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String PASSWORD = "";
    public static final String USERNAME = "";
    private transient CreateJournalHandler handlerUnderTest;

    private static final Context context = new FakeContext();

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private Environment environment;

    @BeforeEach
    void setUp(WireMockRuntimeInfo runtimeInfo) {
        this.environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var dataportenAuthSource = new DataportenAuthClient(httpClient, dataportenBaseUri, USERNAME, PASSWORD);
        var publicationChannelSource =
                new DataportenPublicationChannelClient(httpClient, dataportenBaseUri, dataportenAuthSource);

        handlerUnderTest = new CreateJournalHandler(environment, publicationChannelSource);
    }

    @AfterEach
    void tearDown() throws IOException {
        output.flush();
    }

    @Test
    void shouldReturnCreatedJournalWithSuccess() throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        stubAuth(TOKEN_BODY, HttpURLConnection.HTTP_OK);
        stubResponse(expectedPid, TOKEN_BODY, HttpURLConnection.HTTP_OK);

        handlerUnderTest.handleRequest(constructRequest("Test Journal"), output, context);

        var response = GatewayResponse.fromOutputStream(output, PidDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualPid = response.getBodyObject(PidDto.class);

        var selfUriBase = URI.create(LOCATION);
        assertThat(actualPid, is(equalTo(PidDto.create(selfUriBase, expectedPid))));
    }

    @Test
    void shoudReturnBadGatewayWhenUnautorized() throws IOException {
        var name = "Test Journal";
        var input = constructRequest(name);

        stubAuth(TOKEN_BODY, HttpURLConnection.HTTP_OK);
        stubResponse(null, TOKEN_BODY, HttpURLConnection.HTTP_UNAUTHORIZED);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shoudReturnBadGatewayWhenForbidden() throws IOException {
        var name = "Test Journal";
        var input = constructRequest(name);

        stubAuth(TOKEN_BODY, HttpURLConnection.HTTP_OK);
        stubResponse(null, TOKEN_BODY, HttpURLConnection.HTTP_FORBIDDEN);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shoudReturnBadGatewayWhenInternalServerError() throws IOException {
        var name = "Test Journal";
        var input = constructRequest(name);

        stubAuth(TOKEN_BODY, HttpURLConnection.HTTP_OK);
        stubResponse(null, TOKEN_BODY, HttpURLConnection.HTTP_INTERNAL_ERROR);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(equalTo("Unexpected response from upstream!")));
    }

    @ParameterizedTest
    @ValueSource(ints = {HttpURLConnection.HTTP_UNAUTHORIZED,
            HttpURLConnection.HTTP_INTERNAL_ERROR, HttpURLConnection.HTTP_UNAVAILABLE})
    void shouldReturnBadGatewayWhenAuthResponseNotSuccessful(int httpStatusCode) throws IOException {
        var name = "Test Journal";
        var input = constructRequest(name);

        stubAuth(TOKEN_BODY, httpStatusCode);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(equalTo("Unexpected response from upstream!")));
    }

    private static InputStream constructRequest(String name) throws JsonProcessingException {
        return new HandlerRequestBuilder<CreateJournalRequest>(dtoObjectMapper)
                .withBody(new CreateJournalRequest(name))
                .build();
    }

    private static void stubResponse(String expectedPid, TokenBody token, int statusCode) {
        stubFor(
                post("/createjournal/createpid")
                        .withHeader(HEADER_ACCEPT, WireMock.equalTo("application/json"))
                        .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo("application/json"))
                        .withHeader(HEADER_AUTHORIZATION,
                                WireMock.equalTo(token.getTokenType() + " " + token.getAccessToken()))
                        .willReturn(
                                aResponse()
                                        .withBody(expectedPid)
                                        .withStatus(statusCode)

                        )
        );
    }

    private static void stubAuth(TokenBody token, int statusCode) throws JsonProcessingException {
        stubFor(
                post("/oauth/token")
                        .withBasicAuth(USERNAME, PASSWORD)
                        .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo("x-www-form-urlencoded"))
                        .willReturn(
                                aResponse()
                                        .withBody(dtoObjectMapper.writeValueAsString(token))
                                        .withStatus(statusCode)
                        )
        );
    }
}
