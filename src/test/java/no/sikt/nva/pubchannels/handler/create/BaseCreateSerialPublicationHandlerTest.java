package no.sikt.nva.pubchannels.handler.create;

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
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

public abstract class BaseCreateSerialPublicationHandlerTest extends CreateHandlerTest {

    protected String channelRegistryCreatePathElement;
    protected String channelRegistryFetchPathElement;
    protected String type;
    protected String customChannelPath;

    protected abstract CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> createHandler(
        Environment environment, ChannelRegistryClient channelRegistryClient);

    @Test
    void shouldThrowUnauthorizedIfNotUser() throws IOException {
        var requestBody = new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build();
        handlerUnderTest.handleRequest(constructUnauthorizedRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_UNAUTHORIZED)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Unauthorized")));
    }

    @Test
    void shouldReturnBadGatewayWhenUnauthorized() throws IOException {
        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());
        var request = new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, null, null);

        stubPostResponse(null, request, HttpURLConnection.HTTP_UNAUTHORIZED, channelRegistryCreatePathElement);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadRequestWithOriginalErrorMessageWhenBadRequestFromChannelRegisterApi() throws IOException {
        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());
        var request = new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, null, null);

        stubBadRequestResponse(request, channelRegistryCreatePathElement);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(response.getBodyObject(Problem.class).getDetail(), containsString(PROBLEM));
    }

    @Test
    void shouldReturnBadGatewayWhenForbidden() throws IOException {
        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());

        var request = new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, null, null);
        stubPostResponse(null, request, HttpURLConnection.HTTP_FORBIDDEN, channelRegistryCreatePathElement);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadGatewayWhenInternalServerError() throws IOException {
        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());

        stubPostResponse(null,
                         new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, null, null),
                         HttpURLConnection.HTTP_INTERNAL_ERROR, channelRegistryCreatePathElement);

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
        this.handlerUnderTest = createHandler(environment, setupInteruptedClient());

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

    @Test
    void shouldReturnCreatedChannelWithSuccess() throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        var channelRegistryRequest = new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, null, null);
        stubPostResponse(expectedPid, channelRegistryRequest, HttpURLConnection.HTTP_CREATED,
                         channelRegistryCreatePathElement);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, type).withName(
            VALID_NAME);
        stubFetchOKResponse(testChannel, channelRegistryFetchPathElement);

        var requestBody = new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation,
                   is(equalTo(createPublicationChannelUri(expectedPid, customChannelPath, currentYear()))));

        var expectedChannel = testChannel.asSerialPublicationDto(baseUri, currentYear());
        assertThat(response.getBodyObject(SerialPublicationDto.class), is(equalTo(expectedChannel)));
    }

    @ParameterizedTest(name = "Should create series for print ISSN \"{0}\"")
    @MethodSource("validIssn")
    void shouldCreateChannelWithNameAndPrintIssn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        var clientRequest = new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, issn, null, null);
        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED, channelRegistryCreatePathElement);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, type).withName(VALID_NAME)
                              .withPrintIssn(issn);
        stubFetchOKResponse(testChannel, channelRegistryFetchPathElement);

        var requestBody =
            new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).withPrintIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @ParameterizedTest(name = "Should create series for online ISSN \"{0}\"")
    @MethodSource("validIssn")
    void shouldCreateChannelWithNameAndOnlineIssn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var clientRequest = new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, issn, null);

        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED, channelRegistryCreatePathElement);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, type).withName(VALID_NAME)
                              .withOnlineIssn(issn);
        stubFetchOKResponse(testChannel, channelRegistryFetchPathElement);

        var requestBody =
            new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).withOnlineIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @Test
    void shouldCreateChannelWithNameAndHomepage() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var homepage = "https://a.valid.url.com";
        var clientRequest = new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, null, homepage);

        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED, channelRegistryCreatePathElement);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, type)
                              .withName(VALID_NAME)
                              .withSameAs(
                                  URI.create(homepage));
        stubFetchOKResponse(testChannel, channelRegistryFetchPathElement);

        var requestBody =
            new CreateSerialPublicationRequestBuilder()
                .withName(VALID_NAME)
                .withHomepage(homepage)
                .build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    private static void stubPostResponse(String expectedPid,
                                         ChannelRegistryCreateSerialPublicationRequest request,
                                         int clientResponseHttpCode, String channelRegistryPathElement)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(clientResponseHttpCode,
                     channelRegistryPathElement + "createpid",
                     dtoObjectMapper.writeValueAsString(new CreateChannelResponse(expectedPid)),
                     dtoObjectMapper.writeValueAsString(request));
    }

    private static void stubFetchOKResponse(TestChannel testChannel, String channelRegistryPathElement) {
        var channelRegistryResponse = testChannel.asChannelRegistrySerialPublicationBody();
        var requestUrl = channelRegistryPathElement + testChannel.identifier() + "/" + testChannel.year();
        stubGetResponse(HttpURLConnection.HTTP_OK, requestUrl, channelRegistryResponse);
    }

    private static void stubBadRequestResponse(ChannelRegistryCreateSerialPublicationRequest request,
                                               String channelRegistryPathElement)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                     channelRegistryPathElement + "createpid",
                     dtoObjectMapper.writeValueAsString(PROBLEM),
                     dtoObjectMapper.writeValueAsString(request));
    }
}
