package no.sikt.nva.pubchannels.handler.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.model.TokenBodyResponse;
import no.sikt.nva.pubchannels.handler.create.series.CreateSeriesRequest;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class CreateHandlerTest {
    protected static final String PASSWORD = "";
    protected static final String USERNAME = "";
    protected static final String VALID_NAME = "Valid Name";
    protected static final Context context = new FakeContext();
    protected final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private static final TokenBodyResponse TOKEN_BODY = new TokenBodyResponse("token1", "Bearer");

    @AfterEach
    void tearDown() throws IOException {
        output.flush();
    }

    protected static Stream<String> invalidNames() {
        return Stream.of("name", "abcdefghi ".repeat(31));
    }

    protected static Stream<String> invalidIssn() {
        return Stream.of("123456789", "1234-12XX", "1", "kdsnf0392ujrkijdf");
    }

    protected static Stream<String> validIssn() {
        return Stream.of("0317-8471", "1050-124X");
    }

    protected static Stream<String> invalidUri() {
        return Stream.of("httpss://whatever", "htp://", "fttp://");
    }

    protected static DataportenPublicationChannelClient setupInteruptedClient()
            throws IOException, InterruptedException {
        var httpAuthClient = mock(HttpClient.class);
        when(httpAuthClient.send(any(), any())).thenThrow(new InterruptedException());
        var dataportenAuthBaseUri = URI.create("https://localhost:9898");
        var dataportenAuthClient =
                new DataportenAuthClient(httpAuthClient, dataportenAuthBaseUri, null, null);
        var httpPublicationChannelClient = mock(HttpClient.class);
        return new DataportenPublicationChannelClient(httpPublicationChannelClient,
                dataportenAuthBaseUri,
                dataportenAuthClient);
    }

    protected static <T> InputStream constructRequest(T body) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<T>(dtoObjectMapper)
                .withCurrentCustomer(customerId)
                .withAccessRights(customerId, AccessRight.USER.toString())
                .withBody(body)
                .build();
    }

    protected static <T> InputStream constructUnauthorizedRequest(T body) throws JsonProcessingException {
        return new HandlerRequestBuilder<T>(dtoObjectMapper)
                   .withBody(body)
                   .build();
    }

    protected static void stubResponse(int statusCode, String url, String body, String request) {
        stubFor(
                post(url)
                        .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(HttpHeaders.CONTENT_TYPE_APPLICATION_JSON))
                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                WireMock.equalTo(HttpHeaders.CONTENT_TYPE_APPLICATION_JSON))
                        .withHeader(HttpHeaders.AUTHORIZATION,
                                WireMock.equalTo(TOKEN_BODY.getTokenType() + " " + TOKEN_BODY.getAccessToken()))
                        .withRequestBody(equalToJson(request))
                        .willReturn(
                                aResponse()
                                        .withBody(body)
                                        .withStatus(statusCode)

                        )
        );
    }

    protected static void stubAuth(int statusCode) throws JsonProcessingException {
        stubFor(
                post("/oauth/token")
                        .withBasicAuth(USERNAME, PASSWORD)
                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                WireMock.equalTo(HttpHeaders.CONTENT_TYPE_X_WWW_FORM_URLENCODED))
                        .willReturn(
                                aResponse()
                                        .withBody(dtoObjectMapper.writeValueAsString(TOKEN_BODY))
                                        .withStatus(statusCode)
                        )
        );
    }
}
