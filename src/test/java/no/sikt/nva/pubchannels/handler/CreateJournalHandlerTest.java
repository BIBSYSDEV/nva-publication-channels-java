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

    private transient CreateJournalHandler handlerUnderTest;

    private static final Context context = new FakeContext();

    private transient Environment environment;

    @BeforeEach
    void setUp(WireMockRuntimeInfo runtimeInfo) {
        this.environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var dataportenAuthSource = new DataportenAuthClient(httpClient, dataportenBaseUri, "", "");
        var publicationChannelSource =
                new DataportenPublicationChannelClient(httpClient, dataportenBaseUri, dataportenAuthSource);

        handlerUnderTest = new CreateJournalHandler(environment, publicationChannelSource);

    }

    @Test
    void shouldReturnCreatedJournalWithSuccess() throws IOException {
        var name = "Test Journal";
        InputStream input = constructRequest(name);
        var output = new ByteArrayOutputStream();
        var expectedPid = UUID.randomUUID().toString();

        TokenBody token = new TokenBody("token1", "Bearer");
        stubAuth(token, HttpURLConnection.HTTP_OK);
        stubResponse(expectedPid, token, HttpURLConnection.HTTP_OK);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PidDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualPid = response.getBodyObject(PidDto.class);

        URI selfUriBase = URI.create("https://localhost/findjournal");
        assertThat(actualPid, is(equalTo(PidDto.create(selfUriBase, expectedPid))));
    }

    @Test
    void shoudReturnBadGatewayWhenUnautorized() throws IOException {
        var name = "Test Journal";
        InputStream input = constructRequest(name);
        var output = new ByteArrayOutputStream();

        TokenBody token = new TokenBody("token1", "Bearer");
        stubAuth(token, HttpURLConnection.HTTP_OK);
        stubResponse(null, token, HttpURLConnection.HTTP_UNAUTHORIZED);

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
        InputStream input = constructRequest(name);
        var output = new ByteArrayOutputStream();

        TokenBody token = new TokenBody("token1", "Bearer");
        stubAuth(token, HttpURLConnection.HTTP_OK);
        stubResponse(null, token, HttpURLConnection.HTTP_FORBIDDEN);

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
        InputStream input = constructRequest(name);
        var output = new ByteArrayOutputStream();

        TokenBody token = new TokenBody("token1", "Bearer");
        stubAuth(token, HttpURLConnection.HTTP_OK);
        stubResponse(null, token, HttpURLConnection.HTTP_INTERNAL_ERROR);

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
        InputStream input = constructRequest(name);
        var output = new ByteArrayOutputStream();

        TokenBody token = new TokenBody("token1", "Bearer");
        stubAuth(token, httpStatusCode);

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
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .withHeader("Content-Type", WireMock.equalTo("application/json"))
                        .withHeader("Authorization",
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
                        .withBasicAuth("", "")
                        .withHeader("Content-Type", WireMock.equalTo("x-www-form-urlencoded"))
                        .willReturn(
                                aResponse()
                                        .withBody(dtoObjectMapper.writeValueAsString(token))
                                        .withStatus(statusCode)
                        )
        );
    }
}
