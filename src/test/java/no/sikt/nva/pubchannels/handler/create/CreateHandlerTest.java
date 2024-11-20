package no.sikt.nva.pubchannels.handler.create;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.model.TokenBodyResponse;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;

import nva.commons.core.Environment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.stream.Stream;

@WireMockTest(httpsEnabled = true)
public abstract class CreateHandlerTest {
    protected static final String PASSWORD = "";
    protected static final String USERNAME = "";
    protected static final String VALID_NAME = "Valid Name";
    protected static final String PROBLEM = "Some problem";
    protected static final Context context = new FakeContext();
    private static final TokenBodyResponse TOKEN_BODY = new TokenBodyResponse("token1", "Bearer");
    protected static Environment environment;
    protected final ByteArrayOutputStream output = new ByteArrayOutputStream();
    protected CreateHandler<?, ?> handlerUnderTest;
    protected ChannelRegistryClient publicationChannelClient;
    protected URI baseUri;

    protected static void stubAuth(int statusCode) throws JsonProcessingException {
        stubFor(
                post("/oauth/token")
                        .withBasicAuth(USERNAME, PASSWORD)
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE,
                                WireMock.equalTo(HttpHeaders.CONTENT_TYPE_X_WWW_FORM_URLENCODED))
                        .willReturn(
                                aResponse()
                                        .withStatus(statusCode)
                                        .withBody(dtoObjectMapper.writeValueAsString(TOKEN_BODY))));
    }

    protected static void stubResponse(int statusCode, String url, String body, String request) {
        stubFor(
                post(url)
                        .withHeader(
                                HttpHeaders.ACCEPT,
                                WireMock.equalTo(HttpHeaders.CONTENT_TYPE_APPLICATION_JSON))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE,
                                WireMock.equalTo(HttpHeaders.CONTENT_TYPE_APPLICATION_JSON))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                WireMock.equalTo(
                                        TOKEN_BODY.getTokenType()
                                                + " "
                                                + TOKEN_BODY.getAccessToken()))
                        .withRequestBody(equalToJson(request))
                        .willReturn(aResponse().withStatus(statusCode).withBody(body)));
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

    protected static ChannelRegistryClient setupInteruptedClient()
            throws IOException, InterruptedException {
        var httpAuthClient = mock(HttpClient.class);
        when(httpAuthClient.send(any(), any())).thenThrow(new InterruptedException());
        var dataportenAuthBaseUri = URI.create("https://localhost:9898");
        var dataportenAuthClient =
                new DataportenAuthClient(httpAuthClient, dataportenAuthBaseUri, null, null);
        var httpPublicationChannelClient = mock(HttpClient.class);
        return new ChannelRegistryClient(
                httpPublicationChannelClient, dataportenAuthBaseUri, dataportenAuthClient);
    }

    protected static <T> InputStream constructRequest(T body) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<T>(dtoObjectMapper)
                .withCurrentCustomer(customerId)
                .withBody(body)
                .build();
    }

    protected static <T> InputStream constructUnauthorizedRequest(T body)
            throws JsonProcessingException {
        return new HandlerRequestBuilder<T>(dtoObjectMapper).withBody(body).build();
    }

    protected static void stubGetResponse(int statusCode, String url, String body) {
        stubFor(
                get(url).withHeader(
                                HttpHeaders.ACCEPT,
                                WireMock.equalTo(HttpHeaders.CONTENT_TYPE_APPLICATION_JSON))
                        .willReturn(
                                aResponse()
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                HttpHeaders.CONTENT_TYPE_APPLICATION_JSON_UTF8)
                                        .withStatus(statusCode)
                                        .withBody(body)));
    }

    @BeforeAll
    protected static void beforeAll() {
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);
    }

    @AfterEach
    protected void afterEach() throws IOException {
        output.flush();
    }

    @BeforeEach
    void beforeEach(WireMockRuntimeInfo runtimeInfo) {
        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var dataportenAuthSource =
                new DataportenAuthClient(httpClient, dataportenBaseUri, USERNAME, PASSWORD);
        publicationChannelClient =
                new ChannelRegistryClient(httpClient, dataportenBaseUri, dataportenAuthSource);
    }
}
