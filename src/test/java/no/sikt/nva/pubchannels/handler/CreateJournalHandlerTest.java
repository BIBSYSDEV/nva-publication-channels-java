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
import no.sikt.nva.pubchannels.model.CreateJournalRequestBuilder;
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

    public static final TokenBody TOKEN_BODY = new TokenBody("token1", "Bearer");
    public static final String LOCATION = "Location";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String PASSWORD = "";
    public static final String USERNAME = "";
    public static final String VALID_NAME = "Valid Name";
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

        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(expectedPid, HttpURLConnection.HTTP_OK);

        var testJournal = new CreateJournalRequestBuilder().name("Test Journal").build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));


        var actualLocation = URI.create(response.getHeaders().get(LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid))));
    }


    @Test
    void shoudReturnBadGatewayWhenUnautorized() throws IOException {
        var name = "Test Journal";
        var input = constructRequest(new CreateJournalRequestBuilder().name(name).build());

        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(null, HttpURLConnection.HTTP_UNAUTHORIZED);

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
        var input = constructRequest(new CreateJournalRequestBuilder().name(name).build());

        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(null, HttpURLConnection.HTTP_FORBIDDEN);

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
        var input = constructRequest(new CreateJournalRequestBuilder().name(name).build());

        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(null, HttpURLConnection.HTTP_INTERNAL_ERROR);

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
        var input = constructRequest(new CreateJournalRequestBuilder().name(name).build());

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

        var name = "Test Journal";
        var input = constructRequest(new CreateJournalRequestBuilder().name(name).build());

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

        var testJournal = new CreateJournalRequestBuilder().name(name).build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Name is too")));
    }

    @ParameterizedTest
    @MethodSource("invalidIssn")
    void shouldReturnBadRequestWhenInvalidPissn(String issn) throws IOException {
        var testJournal = new CreateJournalRequestBuilder().name(VALID_NAME)
                .pissn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("PIssn has an invalid ISSN format")));

    }

    @ParameterizedTest
    @MethodSource("validIssn")
    void shouldReturnCreatedWhenValidPissn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(expectedPid, HttpURLConnection.HTTP_OK);

        var testJournal = new CreateJournalRequestBuilder()
                .name(VALID_NAME)
                .pissn(issn)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid))));
    }

    @ParameterizedTest
    @MethodSource("invalidIssn")
    void shouldReturnBadRequestWhenInvalidEissn(String issn) throws IOException {
        var testJournal = new CreateJournalRequestBuilder()
                .name(VALID_NAME)
                .eissn(issn)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("EIssn has an invalid ISSN format")));

    }

    @ParameterizedTest
    @MethodSource("invalidUri")
    void shouldReturnBadRequestWhenInvalidUrl(String url) throws IOException {
        var testJournal = new CreateJournalRequestBuilder()
                .name(VALID_NAME)
                .url(url)
                .build();
        handlerUnderTest.handleRequest(constructRequest(testJournal), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Url has an invalid URL format")));

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

    private static void stubResponse(String expectedPid, int statusCode) {
        stubFor(
                post("/createjournal/createpid")
                        .withHeader(HEADER_ACCEPT, WireMock.equalTo("application/json"))
                        .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo("application/json"))
                        .withHeader(HEADER_AUTHORIZATION,
                                WireMock.equalTo(TOKEN_BODY.getTokenType() + " " + TOKEN_BODY.getAccessToken()))
                        .willReturn(
                                aResponse()
                                        .withBody(expectedPid)
                                        .withStatus(statusCode)

                        )
        );
    }

    private static void stubAuth(int statusCode) throws JsonProcessingException {
        stubFor(
                post("/oauth/token")
                        .withBasicAuth(USERNAME, PASSWORD)
                        .withHeader(HEADER_CONTENT_TYPE, WireMock.equalTo("x-www-form-urlencoded"))
                        .willReturn(
                                aResponse()
                                        .withBody(dtoObjectMapper.writeValueAsString(TOKEN_BODY))
                                        .withStatus(statusCode)
                        )
        );
    }
}
