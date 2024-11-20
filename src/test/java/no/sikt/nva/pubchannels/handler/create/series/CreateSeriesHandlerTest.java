package no.sikt.nva.pubchannels.handler.create.series;

import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_PATH;
import static no.sikt.nva.pubchannels.handler.TestChannel.createEmptyTestChannel;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.currentYear;
import static no.sikt.nva.pubchannels.handler.TestUtils.currentYearAsInteger;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSeriesRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.create.CreateHandlerTest;
import no.sikt.nva.pubchannels.handler.create.journal.CreateJournalRequestBuilder;
import no.sikt.nva.pubchannels.handler.model.SeriesDto;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

class CreateSeriesHandlerTest extends CreateHandlerTest {

    @BeforeEach
    void setUp() {
        handlerUnderTest = new CreateSeriesHandler(environment, publicationChannelClient);
        baseUri = UriWrapper.fromHost(environment.readEnv("API_DOMAIN"))
                            .addChild(CUSTOM_DOMAIN_BASE_PATH)
                            .addChild(SERIES_PATH)
                            .getUri();
    }

    @Test
    void shouldReturnCreatedJournalWithSuccess() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var request = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, null, null);

        stubPostResponse(expectedPid, request, HttpURLConnection.HTTP_CREATED);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, "series").withName(VALID_NAME);
        stubFetchOKResponse(testChannel);

        var requestBody = new CreateSeriesRequestBuilder().withName(VALID_NAME).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SeriesDto.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createPublicationChannelUri(expectedPid, SERIES_PATH, currentYear()))));

        var expectedSeries = testChannel.asSeriesDto(baseUri, currentYear());
        assertThat(response.getBodyObject(SeriesDto.class), is(equalTo(expectedSeries)));
    }

    @Test
    void shouldReturnBadGatewayWhenUnauthorized() throws IOException {
        var input = constructRequest(new CreateSeriesRequestBuilder().withName(VALID_NAME).build());
        var request = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, null, null);

        stubPostResponse(null, request, HttpURLConnection.HTTP_UNAUTHORIZED);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadRequestWithOriginalErrorMessageWhenBadRequestFromChannelRegisterApi() throws IOException {
        var input = constructRequest(new CreateJournalRequestBuilder().withName(VALID_NAME).build());
        var request = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, null, null);

        setupBadRequestStub(request);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(response.getBodyObject(Problem.class).getDetail(), containsString(PROBLEM));
    }

    @Test
    void shouldReturnBadGatewayWhenForbidden() throws IOException {
        var input = constructRequest(new CreateSeriesRequestBuilder().withName(VALID_NAME).build());

        var request = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, null, null);
        stubPostResponse(null, request, HttpURLConnection.HTTP_FORBIDDEN);

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
        var input = constructRequest(new CreateSeriesRequestBuilder().withName(VALID_NAME).build());

        stubPostResponse(null,
                         new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, null, null),
                         HttpURLConnection.HTTP_INTERNAL_ERROR);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream!")));
    }

    @ParameterizedTest(name = "Should return BadGateway for response code \"{0}\"")
    @ValueSource(ints = {HttpURLConnection.HTTP_UNAUTHORIZED,
        HttpURLConnection.HTTP_INTERNAL_ERROR, HttpURLConnection.HTTP_UNAVAILABLE})
    void shouldReturnBadGatewayWhenAuthResponseNotSuccessful(int httpStatusCode) throws IOException {
        var input = constructRequest(new CreateSeriesRequestBuilder().withName(VALID_NAME).build());

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
        this.handlerUnderTest = new CreateSeriesHandler(environment, setupInteruptedClient());

        var input = constructRequest(new CreateSeriesRequestBuilder().withName(VALID_NAME).build());

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Unable to reach upstream!"));
        assertThat(appender.getMessages(), containsString(InterruptedException.class.getSimpleName()));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(equalTo("Unable to reach upstream!")));
    }

    @ParameterizedTest(name = "Should return BadRequest for invalid name \"{0}\"")
    @MethodSource("invalidNames")
    void shouldReturnBadRequestWhenNameInvalid(String name) throws IOException {

        var requestBody = new CreateSeriesRequestBuilder().withName(name).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Name is too")));
    }

    @ParameterizedTest(name = "Should return BadRequest for invalid print ISSN \"{0}\"")
    @MethodSource("invalidIssn")
    void shouldReturnBadRequestWhenInvalidPissn(String issn) throws IOException {
        var requestBody =
            new CreateSeriesRequestBuilder().withName(VALID_NAME).withPrintIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("PrintIssn has an invalid ISSN format")));
    }

    @ParameterizedTest(name = "Should return BadRequest for invalid online ISSN \"{0}\"")
    @MethodSource("invalidIssn")
    void shouldReturnBadRequestWhenInvalidEissn(String issn) throws IOException {
        var requestBody =
            new CreateSeriesRequestBuilder().withName(VALID_NAME).withOnlineIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("OnlineIssn has an invalid ISSN format")));
    }

    @ParameterizedTest(name = "Should return BadRequest for invalid URL \"{0}\"")
    @MethodSource("invalidUri")
    void shouldReturnBadRequestWhenInvalidUrl(String url) throws IOException {
        var requestBody =
            new CreateSeriesRequestBuilder().withName(VALID_NAME).withHomepage(url).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Homepage has an invalid URL format")));
    }

    @ParameterizedTest(name = "Should create series for print ISSN \"{0}\"")
    @MethodSource("validIssn")
    void shouldCreateSeriesWithNameAndPrintIssn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        var clientRequest = new ChannelRegistryCreateSeriesRequest(VALID_NAME, issn, null, null);
        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, "series").withName(VALID_NAME);
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSeriesRequestBuilder().withName(VALID_NAME).withPrintIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SeriesDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @ParameterizedTest(name = "Should create series for online ISSN \"{0}\"")
    @MethodSource("validIssn")
    void shouldCreateSeriesWithNameAndOnlineIssn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var clientRequest = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, issn, null);

        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, "series").withName(VALID_NAME)
                                                                                               .withOnlineIssn(issn);
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSeriesRequestBuilder().withName(VALID_NAME).withOnlineIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SeriesDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }


    @Test
    void shouldCreateSeriesWithNameAndHomepage() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var homepage = "https://a.valid.url.com";
        var clientRequest = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, null, homepage);

        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, "series")
                              .withName(VALID_NAME)
                              .withSameAs(URI.create(homepage));
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSeriesRequestBuilder()
                .withName(VALID_NAME)
                .withHomepage(homepage)
                .build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SeriesDto.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @Test
    void shouldThrowUnauthorizedIfNotUser() throws IOException {
        var requestBody = new CreateSeriesRequestBuilder().withName(VALID_NAME).build();
        handlerUnderTest.handleRequest(constructUnauthorizedRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_UNAUTHORIZED)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Unauthorized")));
    }

    private static void stubFetchOKResponse(TestChannel testChannel) {
        var channelRegistryResponse = testChannel.asChannelRegistrySeriesBody();
        var requestUrl = "/findseries/" + testChannel.identifier() + "/" + testChannel.year();
        stubGetResponse(HttpURLConnection.HTTP_OK, requestUrl, channelRegistryResponse);
    }

    private static void stubPostResponse(String expectedPid,
                                         ChannelRegistryCreateSeriesRequest request,
                                         int clientResponseHttpCode)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(clientResponseHttpCode, "/createseries/createpid",
                     dtoObjectMapper.writeValueAsString(new CreateChannelResponse(expectedPid)),
                     dtoObjectMapper.writeValueAsString(request));
    }

    private static void setupBadRequestStub(ChannelRegistryCreateSeriesRequest request)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(HttpURLConnection.HTTP_BAD_REQUEST, "/createseries/createpid",
                     dtoObjectMapper.writeValueAsString(PROBLEM),
                     dtoObjectMapper.writeValueAsString(request));
    }

}
