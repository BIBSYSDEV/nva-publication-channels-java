package no.sikt.nva.pubchannels.handler.create.publisher;

import static no.sikt.nva.pubchannels.handler.TestUtils.createExpectedUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.validIsbnPrefix;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreatePublisherRequest;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreatePublisherResponse;
import no.sikt.nva.pubchannels.handler.create.CreateHandlerTest;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
class CreatePublisherHandlerTest extends CreateHandlerTest {

    public static final String PUBLISHER_PATH_ELEMENT = "publisher";
    private transient CreatePublisherHandler handlerUnderTest;

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

        handlerUnderTest = new CreatePublisherHandler(environment, publicationChannelSource);
    }

    @Test
    void shouldReturnCreatedPublisherWithSuccess() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var request = new DataportenCreatePublisherRequest(VALID_NAME, null, null);
        var testPublisher = new CreatePublisherRequestBuilder().withName(VALID_NAME).build();

        setupStub(expectedPid, request, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);

        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid, PUBLISHER_PATH_ELEMENT))));
    }

    @Test
    void shouldReturnBadGatewayWhenUnauthorized() throws IOException {
        var input = constructRequest(new CreatePublisherRequestBuilder().withName(VALID_NAME).build());
        var request = new DataportenCreatePublisherRequest(VALID_NAME, null, null);

        setupStub(null, request, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_UNAUTHORIZED);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadGatewayWhenForbidden() throws IOException {
        var input = constructRequest(new CreatePublisherRequestBuilder().withName(VALID_NAME).build());

        var request = new DataportenCreatePublisherRequest(VALID_NAME, null, null);
        setupStub(null, request, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_FORBIDDEN);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadGatewayWhenInternalServerError() throws IOException {
        var input = constructRequest(new CreatePublisherRequestBuilder().withName(VALID_NAME).build());

        setupStub(null, new DataportenCreatePublisherRequest(VALID_NAME, null, null),
                  HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_INTERNAL_ERROR);

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
        var input = constructRequest(new CreatePublisherRequestBuilder().withName(VALID_NAME).build());

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
        this.handlerUnderTest = new CreatePublisherHandler(environment, setupInteruptedClient());

        var input = constructRequest(new CreatePublisherRequestBuilder().withName(VALID_NAME).build());

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

        var testPublisher = new CreatePublisherRequestBuilder().withName(name).build();
        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Name is too")));
    }

    @ParameterizedTest
    @MethodSource("invalidIsbnPrefixes")
    void shouldReturnBadRequestWhenIsbnPrefixInvalid(String isbnPrefix) throws IOException {

        var testPublisher = new CreatePublisherRequestBuilder().withName(randomString())
                                .withIsbnPrefix(isbnPrefix)
                                .build();
        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Isbn prefix")));
    }

    @ParameterizedTest
    @MethodSource("invalidUri")
    void shouldReturnBadRequestWhenInvalidUrl(String url) throws IOException {
        var testPublisher = new CreatePublisherRequestBuilder()
                                .withName(VALID_NAME)
                                .withHomepage(url)
                                .build();
        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Homepage has an invalid URL format")));
    }

    @Test
    void shouldCreatePublisherWithNameAndIsbnPrefix() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var isbnPrefix = String.valueOf(validIsbnPrefix());
        var clientRequest = new DataportenCreatePublisherRequest(VALID_NAME, isbnPrefix, null);

        setupStub(expectedPid, clientRequest, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);

        var testPublisher = new CreatePublisherRequestBuilder()
                                .withName(VALID_NAME)
                                .withIsbnPrefix(isbnPrefix)
                                .build();
        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid, PUBLISHER_PATH_ELEMENT))));
    }

    @Test
    void shouldCreatePublisherWithNameAndHomepage() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var homepage = "https://a.valid.url.com";
        var clientRequest = new DataportenCreatePublisherRequest(VALID_NAME, null, homepage);

        setupStub(expectedPid, clientRequest, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);

        var testPublisher = new CreatePublisherRequestBuilder()
                                .withName(VALID_NAME)
                                .withHomepage(homepage)
                                .build();
        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);

        var response = GatewayResponse.fromOutputStream(output, Void.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createExpectedUri(expectedPid, PUBLISHER_PATH_ELEMENT))));
    }

    @Test
    void shouldThrowUnauthorizedIfNotUser() throws IOException {
        var testPublisher = new CreatePublisherRequestBuilder()
                                .withName(VALID_NAME)
                                .build();
        var request = new HandlerRequestBuilder<CreatePublisherRequest>(dtoObjectMapper)
                          .withBody(testPublisher)
                          .build();
        handlerUnderTest.handleRequest(request, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_UNAUTHORIZED)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Unauthorized")));
    }

    private static Stream<String> invalidIsbnPrefixes() {
        return Stream.of("12345678912345", "978-12345-1234567", "String-String", randomString());
    }

    private void setupStub(
        String expectedPid,
        DataportenCreatePublisherRequest request,
        int clientAuthResponseHttpCode,
        int clientResponseHttpCode)
        throws JsonProcessingException {
        stubAuth(clientAuthResponseHttpCode);
        stubResponse(clientResponseHttpCode, "/createpublisher/createpid",
                     dtoObjectMapper.writeValueAsString(new DataportenCreatePublisherResponse(expectedPid)),
                     dtoObjectMapper.writeValueAsString(request));
    }
}
