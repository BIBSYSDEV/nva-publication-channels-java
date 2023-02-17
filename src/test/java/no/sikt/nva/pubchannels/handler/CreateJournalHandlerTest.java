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
import org.mockito.Mockito;

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
        InputStream input = new HandlerRequestBuilder<CreateJournalRequest>(dtoObjectMapper)
                .withBody(new CreateJournalRequest(name))
                .build();
        var output = new ByteArrayOutputStream();
        var expectedPid = UUID.randomUUID().toString();

        TokenBody token = new TokenBody("token1", "Bearer");
        stubAuth(token);
        stubResponse(expectedPid, token);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PidDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualPid = response.getBodyObject(PidDto.class);

        URI selfUriBase = URI.create("https://localhost/findjournal");
        assertThat(actualPid, is(equalTo(PidDto.create(selfUriBase, expectedPid))));
    }

    private static void stubResponse(String expectedPid, TokenBody token) {
        stubFor(
                post("/createjournal/createpid")
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .withHeader("Content-Type", WireMock.equalTo("application/json"))
                        .withHeader("Authorization",
                                WireMock.equalTo(token.getTokenType() + " " + token.getAccessToken()))
                        .willReturn(
                                aResponse()
                                        .withBody(expectedPid)
                                        .withStatus(HttpURLConnection.HTTP_OK)

                        )
        );
    }

    private static void stubAuth(TokenBody token) throws JsonProcessingException {
        stubFor(
                post("/oauth/token")
                        .withBasicAuth("", "")
                        .withHeader("Content-Type", WireMock.equalTo("x-www-form-urlencoded"))
                        .willReturn(
                                aResponse()
                                        .withBody(dtoObjectMapper.writeValueAsString(token))
                                        .withStatus(HttpURLConnection.HTTP_OK)
                        )
        );
    }
}
