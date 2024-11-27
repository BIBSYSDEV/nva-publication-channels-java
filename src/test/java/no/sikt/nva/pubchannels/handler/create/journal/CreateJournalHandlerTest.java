package no.sikt.nva.pubchannels.handler.create.journal;

import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
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
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateJournalRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.create.BaseCreateSerialPublicationHandlerTest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

class CreateJournalHandlerTest extends BaseCreateSerialPublicationHandlerTest {

    @BeforeEach
    void setUp() {
        handlerUnderTest = new CreateJournalHandler(environment, publicationChannelClient);
        baseUri = UriWrapper.fromHost(environment.readEnv("API_DOMAIN"))
                      .addChild(CUSTOM_DOMAIN_BASE_PATH)
                      .addChild(JOURNAL_PATH)
                      .getUri();
        channelRegistryPathElement = "/createjournal/";
    }

    @Test
    void shouldReturnCreatedJournalWithSuccess() throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        var channelRegistryRequest = new ChannelRegistryCreateJournalRequest(VALID_NAME, null, null, null);
        stubPostResponse(expectedPid, channelRegistryRequest, HttpURLConnection.HTTP_CREATED, "/createjournal/");

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, JOURNAL_TYPE).withName(
            VALID_NAME);
        stubFetchOKResponse(testChannel);

        var requestBody = new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createPublicationChannelUri(expectedPid, JOURNAL_PATH, currentYear()))));

        var expectedJournal = testChannel.asSerialPublicationDto(baseUri, currentYear());
        assertThat(response.getBodyObject(SerialPublicationDto.class), is(equalTo(expectedJournal)));
    }

    @Test
    void shouldReturnBadGatewayWhenInternalServerError() throws IOException {
        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());

        stubPostResponse(null,
                         new ChannelRegistryCreateJournalRequest(VALID_NAME, null, null, null),
                         HttpURLConnection.HTTP_INTERNAL_ERROR, "/createjournal/");

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @ParameterizedTest(name = "Should return BadGateway for response code \"{0}\"")
    @ValueSource(ints = {HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_INTERNAL_ERROR,
        HttpURLConnection.HTTP_UNAVAILABLE})
    void shouldReturnBadGatewayWhenAuthResponseNotSuccessful(int httpStatusCode) throws IOException {
        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());

        stubAuth(httpStatusCode);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadGatewayWhenAuthClientInterruptionOccurs() throws IOException, InterruptedException {
        this.handlerUnderTest = new CreateJournalHandler(environment, setupInteruptedClient());

        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());

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

        var requestBody = new CreateSerialPublicationRequestBuilder().withName(name).build();
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
            new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).withPrintIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("PrintIssn has an invalid ISSN format")));
    }

    @ParameterizedTest(name = "Should return BadRequest for invalid online ISSN \"{0}\"")
    @MethodSource("invalidIssn")
    void shouldReturnBadRequestWhenInvalidElectronicIssn(String issn) throws IOException {
        var requestBody =
            new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).withOnlineIssn(issn).build();
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
            new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).withHomepage(url).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Homepage has an invalid URL format")));
    }

    @ParameterizedTest(name = "Should create series for print ISSN \"{0}\"")
    @MethodSource("validIssn")
    void shouldCreateJournalWithNameAndPrintIssn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        var clientRequest = new ChannelRegistryCreateJournalRequest(VALID_NAME, issn, null, null);
        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED, "/createjournal/");

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, JOURNAL_TYPE).withName(VALID_NAME)
                              .withPrintIssn(issn);
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).withPrintIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @ParameterizedTest(name = "Should create series for online ISSN \"{0}\"")
    @MethodSource("validIssn")
    void shouldCreateJournalWithNameAndOnlineIssn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var clientRequest = new ChannelRegistryCreateJournalRequest(VALID_NAME, null, issn, null);

        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED, "/createjournal/");

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, JOURNAL_TYPE).withName(VALID_NAME)
                              .withOnlineIssn(issn);
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).withOnlineIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @Test
    void shouldCreateJournalWithNameAndHomepage() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var homepage = "https://a.valid.url.com";
        var clientRequest = new ChannelRegistryCreateJournalRequest(VALID_NAME, null, null, homepage);

        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED, "/createjournal/");

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, JOURNAL_TYPE)
                              .withName(VALID_NAME)
                              .withSameAs(
                                  URI.create(homepage));
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSerialPublicationRequestBuilder()
                .withName(VALID_NAME)
                .withHomepage(homepage)
                .build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    private static void stubFetchOKResponse(TestChannel testChannel) {
        var channelRegistryResponse = testChannel.asChannelRegistryJournalBody();
        var requestUrl = "/findjournal/" + testChannel.identifier() + "/" + testChannel.year();
        stubGetResponse(HttpURLConnection.HTTP_OK, requestUrl, channelRegistryResponse);
    }

    private static void stubPostResponse(String expectedPid,
                                         ChannelRegistryCreateJournalRequest request,
                                         int clientResponseHttpCode, String channelRegistryCustomPathElement)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(clientResponseHttpCode,
                     channelRegistryCustomPathElement + "createpid",
                     dtoObjectMapper.writeValueAsString(new CreateChannelResponse(expectedPid)),
                     dtoObjectMapper.writeValueAsString(request));
    }


}
