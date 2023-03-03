package no.sikt.nva.pubchannels.handler.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.model.DataportenCreateJournalRequest;
import no.sikt.nva.pubchannels.dataporten.model.DataportenCreateJournalResponse;
import no.sikt.nva.pubchannels.dataporten.model.TokenBodyResponse;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WireMockTest(httpsEnabled = true)
class CreateJournalHandlerTest {

    private static final TokenBodyResponse TOKEN_BODY = new TokenBodyResponse("token1", "Bearer");
    private static final String PASSWORD = "";
    private static final String USERNAME = "";
    private static final String VALID_NAME = "Valid Name";
    private transient CreateJournalHandler handlerUnderTest;

    private static final Context context = new FakeContext();

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private Environment environment;

    @BeforeEach
    void setUp(WireMockRuntimeInfo runtimeInfo) {
        this.environment = mock(Environment.class);
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
        var request = new DataportenCreateJournalRequest(VALID_NAME, null, null, null);
        var testJournal = new CreateJournalRequestBuilder().withName(VALID_NAME).build();

        setup(expectedPid, request, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);

        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));


        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid))));
    }

    private void setup(String expectedPid, DataportenCreateJournalRequest request, int clientAuthResponseHttpCode, int clientResponseHttpCode) throws JsonProcessingException {
        stubAuth(clientAuthResponseHttpCode);
        stubResponse(expectedPid, clientResponseHttpCode, request);
    }


    @Test
    void shoudReturnBadGatewayWhenUnautorized() throws IOException {
        var input = constructRequest(new CreateJournalRequestBuilder().withName(VALID_NAME).build());
        var request = new DataportenCreateJournalRequest(VALID_NAME, null, null, null);

        setup(null, request, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_UNAUTHORIZED);

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
        var input = constructRequest(new CreateJournalRequestBuilder().withName(VALID_NAME).build());

        setup(null, new DataportenCreateJournalRequest(VALID_NAME, null, null, null), HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_FORBIDDEN);

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
        var input = constructRequest(new CreateJournalRequestBuilder().withName(VALID_NAME).build());

        setup(null, new DataportenCreateJournalRequest(VALID_NAME, null, null, null), HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_INTERNAL_ERROR);

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
        var input = constructRequest(new CreateJournalRequestBuilder().withName(VALID_NAME).build());

        stubAuth(httpStatusCode);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadGatewayWhenAuthClientInterruptionOccurs() throws IOException, InterruptedException {
        this.handlerUnderTest = new CreateJournalHandler(environment, setupIntteruptedClient());

        var input = constructRequest(new CreateJournalRequestBuilder().withName(VALID_NAME).build());

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Unable to reach upstream!"));
        assertThat(appender.getMessages(), containsString(InterruptedException.class.getSimpleName()));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(equalTo("Unable to reach upstream!")));
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void shouldReturnBadRequestWhenNameInvalid(String name) throws IOException {

        var testJournal = new CreateJournalRequestBuilder().withName(name).build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Name is too")));
    }

    @ParameterizedTest
    @MethodSource("invalidIssn")
    void shouldReturnBadRequestWhenInvalidPissn(String issn) throws IOException {
        var testJournal = new CreateJournalRequestBuilder().withName(VALID_NAME)
                .withPrintIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("PrintIssn has an invalid ISSN format")));

    }

    @ParameterizedTest
    @MethodSource("validIssn")
    void shouldReturnCreatedWhenValidPissn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        setup(expectedPid, new DataportenCreateJournalRequest(VALID_NAME, issn, null, null), HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_OK);

        var testJournal = new CreateJournalRequestBuilder()
                .withName(VALID_NAME)
                .withPrintIssn(issn)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid))));
    }

    @ParameterizedTest
    @MethodSource("invalidIssn")
    void shouldReturnBadRequestWhenInvalidEissn(String issn) throws IOException {
        var testJournal = new CreateJournalRequestBuilder()
                .withName(VALID_NAME)
                .withOnlineIssn(issn)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("OnlineIssn has an invalid ISSN format")));

    }

    @ParameterizedTest
    @MethodSource("invalidUri")
    void shouldReturnBadRequestWhenInvalidUrl(String url) throws IOException {
        var testJournal = new CreateJournalRequestBuilder()
                .withName(VALID_NAME)
                .withHomepage(url)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Homepage has an invalid URL format")));

    }

    @Test
    void shouldCreateJournalWithNameAndPrintIssn() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var printIssn = validIssn().findAny().get();
        var clientRequest = new DataportenCreateJournalRequest(VALID_NAME, printIssn, null, null);
        setup(expectedPid, clientRequest, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);


        var testJournal = new CreateJournalRequestBuilder()
                .withName(VALID_NAME)
                .withPrintIssn(printIssn)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));


        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid))));
    }

    @Test
    void shouldCreateJournalWithNameAndOnlineIssn() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var onlineIssn = validIssn().findAny().get();
        var clientRequest = new DataportenCreateJournalRequest(VALID_NAME, null, onlineIssn, null);

        setup(expectedPid,clientRequest,HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);

        var testJournal = new CreateJournalRequestBuilder()
                .withName(VALID_NAME)
                .withOnlineIssn(onlineIssn)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));


        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid))));
    }

    @Test
    void shouldCreateJournalWithNameAndHomepage() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var homepage = "https://a.valid.url.com";
        var clientRequest = new DataportenCreateJournalRequest(VALID_NAME, null, null, homepage);

        setup(expectedPid,clientRequest,HttpURLConnection.HTTP_OK,HttpURLConnection.HTTP_CREATED);

        var testJournal = new CreateJournalRequestBuilder()
                .withName(VALID_NAME)
                .withHomepage(homepage)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));


        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid))));
    }

    static Stream<String> invalidNames() {
        return Stream.of("name", "abcdefghi ".repeat(31));
    }

    static Stream<String> invalidIssn() {
        return Stream.of("123456789", "1234-12XX", "1", "kdsnf0392ujrkijdf");
    }

    static Stream<String> validIssn() {
        return Stream.of("0317-8471", "1050-124X");
    }

    static Stream<String> invalidUri() {
        return Stream.of("httpss://whatever", "htp://", "fttp://");
    }

    private static DataportenPublicationChannelClient setupIntteruptedClient()
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

    private URI createExpectedUri(String pid) {
        return new UriWrapper(HTTPS, "localhost")
                .addChild("publication-channels", "journal", pid)
                .getUri();
    }

    private static InputStream constructRequest(CreateJournalRequest body) throws JsonProcessingException {
        return new HandlerRequestBuilder<CreateJournalRequest>(dtoObjectMapper)
                .withBody(body)
                .build();
    }

    private static void stubResponse(String expectedPid, int statusCode, DataportenCreateJournalRequest request)
            throws JsonProcessingException {
        var body = new DataportenCreateJournalResponse(expectedPid);
        stubFor(
                post("/createjournal/createpid")
                        .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(HttpHeaders.CONTENT_TYPE_APPLICATION_JSON))
                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                WireMock.equalTo(HttpHeaders.CONTENT_TYPE_APPLICATION_JSON))
                        .withHeader(HttpHeaders.AUTHORIZATION,
                                WireMock.equalTo(TOKEN_BODY.getTokenType() + " " + TOKEN_BODY.getAccessToken()))
                        .withRequestBody(equalToJson(dtoObjectMapper.writeValueAsString(request)))
                        .willReturn(
                                aResponse()
                                        .withBody(dtoObjectMapper.writeValueAsString(body))
                                        .withStatus(statusCode)

                        )
        );
    }

    private static void stubAuth(int statusCode) throws JsonProcessingException {
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
