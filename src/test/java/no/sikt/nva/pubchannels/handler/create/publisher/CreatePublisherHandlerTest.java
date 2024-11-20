package no.sikt.nva.pubchannels.handler.create.publisher;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.PUBLISHER_PATH;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublicationChannelUri;
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
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Year;
import java.util.UUID;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreatePublisherRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.TestUtils;
import no.sikt.nva.pubchannels.handler.create.CreateHandlerTest;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
class CreatePublisherHandlerTest extends CreateHandlerTest {

    private static final String PROBLEM = "Some problem";
    private transient CreatePublisherHandler handlerUnderTest;
    private Environment environment;

    @BeforeEach
    void setUp(WireMockRuntimeInfo runtimeInfo) {
        this.environment = mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);

        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var dataportenAuthSource = new DataportenAuthClient(httpClient, dataportenBaseUri, USERNAME, PASSWORD);
        var publicationChannelSource = new ChannelRegistryClient(httpClient, dataportenBaseUri, dataportenAuthSource);

        handlerUnderTest = new CreatePublisherHandler(environment, publicationChannelSource);
    }

    @Test
    void shouldReturnCreatedPublisherWithSuccess() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var request = new ChannelRegistryCreatePublisherRequest(VALID_NAME, null, null);
        var testPublisher = new CreatePublisherRequestBuilder().withName(VALID_NAME).build();

        setupStub(expectedPid, request, HttpURLConnection.HTTP_CREATED);

        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);

        var response = GatewayResponse.fromOutputStream(output, CreatePublisherResponse.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation,
                   is(equalTo(createPublicationChannelUri(expectedPid, PUBLISHER_PATH, TestUtils.currentYear()))));

        var expectedPublisher = constructExpectedPublisher(expectedPid);
        assertThat(response.getBodyObject(CreatePublisherResponse.class), is(equalTo(expectedPublisher)));
    }

    @Test
    void shouldReturnBadGatewayWhenUnauthorized() throws IOException {
        var input = constructRequest(new CreatePublisherRequestBuilder().withName(VALID_NAME).build());
        var request = new ChannelRegistryCreatePublisherRequest(VALID_NAME, null, null);

        setupStub(null, request, HttpURLConnection.HTTP_UNAUTHORIZED);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadRequestWithOriginalErrorMessageWhenBadRequestFromChannelRegisterApi() throws IOException {
        var input = constructRequest(new CreatePublisherRequestBuilder().withName(VALID_NAME).build());
        var request = new ChannelRegistryCreatePublisherRequest(VALID_NAME, null, null);

        setupBadRequestStub(request);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(response.getBodyObject(Problem.class).getDetail(), containsString(PROBLEM));
    }

    @Test
    void shouldReturnBadGatewayWhenForbidden() throws IOException {
        var input = constructRequest(new CreatePublisherRequestBuilder().withName(VALID_NAME).build());

        var request = new ChannelRegistryCreatePublisherRequest(VALID_NAME, null, null);
        setupStub(null, request, HttpURLConnection.HTTP_FORBIDDEN);

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

        setupStub(null,
                  new ChannelRegistryCreatePublisherRequest(VALID_NAME, null, null),
                  HttpURLConnection.HTTP_INTERNAL_ERROR);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream!")));
    }

    @ParameterizedTest
    @ValueSource(ints = {HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_INTERNAL_ERROR,
        HttpURLConnection.HTTP_UNAVAILABLE})
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
        var clientRequest = new ChannelRegistryCreatePublisherRequest(VALID_NAME, isbnPrefix, null);

        setupStub(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED);

        var testPublisher = new CreatePublisherRequestBuilder()
                                .withName(VALID_NAME)
                                .withIsbnPrefix(isbnPrefix)
                                .build();
        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);

        var response = GatewayResponse
                           .fromOutputStream(output, CreatePublisherResponse.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @Test
    void shouldCreatePublisherWithNameAndHomepage() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var homepage = "https://a.valid.url.com";
        var clientRequest = new ChannelRegistryCreatePublisherRequest(VALID_NAME, null, homepage);

        setupStub(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED);

        var testPublisher = new CreatePublisherRequestBuilder()
                                .withName(VALID_NAME)
                                .withHomepage(homepage)
                                .build();
        handlerUnderTest.handleRequest(constructRequest(testPublisher), output, context);

        var response = GatewayResponse
                           .fromOutputStream(output, CreatePublisherResponse.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
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

    private CreatePublisherResponse constructExpectedPublisher(String pid) {
        var uri = UriWrapper.fromHost(environment.readEnv("API_DOMAIN"))
                            .addChild(CUSTOM_DOMAIN_BASE_PATH)
                            .addChild(PUBLISHER_PATH)
                            .addChild(pid)
                            .addChild(Year.now().toString())
                            .getUri();
        return new CreatePublisherResponse(uri, VALID_NAME, null, ScientificValue.UNASSIGNED, null);
    }

    private void setupStub(String expectedPid,
                           ChannelRegistryCreatePublisherRequest request,
                           int clientResponseHttpCode)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(clientResponseHttpCode,
                     "/createpublisher/createpid",
                     dtoObjectMapper.writeValueAsString(new CreateChannelResponse(expectedPid)),
                     dtoObjectMapper.writeValueAsString(request));
        stubFetchResponse(expectedPid);
    }

    private void setupBadRequestStub(ChannelRegistryCreatePublisherRequest request)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(HttpURLConnection.HTTP_BAD_REQUEST, "/createpublisher/createpid",
                     dtoObjectMapper.writeValueAsString(PROBLEM),
                     dtoObjectMapper.writeValueAsString(request));
        stubFetchResponse(null);
    }

    private void stubFetchResponse(String pid) {
        stubFor(
            get("/findpublisher/" + pid + "/" + Year.now())
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(nonNull(pid) ? new ChannelRegistryPublisher(pid,
                                                                              null,
                                                                              null,
                                                                              VALID_NAME,
                                                                              null,
                                                                              null,
                                                                              "publisher").toJsonString() : null)));
    }
}
