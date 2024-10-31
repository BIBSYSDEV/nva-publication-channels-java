package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestCommons.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.sikt.nva.pubchannels.TestCommons.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestCommons.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestCommons.LOCATION;
import static no.sikt.nva.pubchannels.TestCommons.PUBLISHER_PATH;
import static no.sikt.nva.pubchannels.TestCommons.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.*;
import static no.sikt.nva.pubchannels.handler.TestUtils.YEAR_START;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockChannelRegistryResponse;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockRedirectedClient;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockResponseWithHttpStatus;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.sikt.nva.pubchannels.handler.TestUtils.setupInterruptedClient;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.net.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.TestUtils;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
class FetchPublisherByIdentifierAndYearHandlerTest {

    private static final String SELF_URI_BASE = "https://localhost/publication-channels/" + PUBLISHER_PATH;
    private static final String CHANNEL_REGISTRY_PATH_ELEMENT = "/findpublisher/";
    private static final Context context = new FakeContext();
    private FetchPublisherByIdentifierAndYearHandler handlerUnderTest;
    private ByteArrayOutputStream output;
    private Environment environment;
    private String channelRegistryBaseUri;

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);
        channelRegistryBaseUri = runtimeInfo.getHttpsBaseUrl();
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new ChannelRegistryClient(httpClient, URI.create(channelRegistryBaseUri), null);
        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        output.flush();
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);

        var expectedPublisher = mockPublisherFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualPublisher = response.getBodyObject(PublisherDto.class);
        assertThat(actualPublisher, is(equalTo(expectedPublisher)));
    }

    @Test
    void shouldIncludeYearInResponse() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);
        mockPublisherFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);
        var actualYear = response.getBodyObject(PublisherDto.class).getYear();
        assertThat(actualYear, is(equalTo(String.valueOf(year))));
    }

    @ParameterizedTest
    @DisplayName("Should return requested media type")
    @MethodSource("no.sikt.nva.pubchannels.TestCommons#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType) throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, mediaType);

        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedPublisher = mockPublisherFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);

        var actualPublisher = response.getBodyObject(PublisherDto.class);
        assertThat(actualPublisher, is(equalTo(expectedPublisher)));
        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));
        var contentType = response.getHeaders().get(CONTENT_TYPE);
        assertThat(contentType, is(equalTo(expectedMediaType)));
    }

    @Test
    void shouldReturnChannelIdWithRequestedYearIfThirdPartyDoesNotProvideYear() throws IOException {
        var year = String.valueOf(YEAR_START);
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var expectedPublisher = mockPublisherFoundYearValueNull(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualPublisher = response.getBodyObject(PublisherDto.class);
        assertThat(actualPublisher, is(equalTo(expectedPublisher)));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("invalidYearsProvider")
    void shouldReturnBadRequestWhenPathParameterYearIsNotValid(String year) throws IOException {

        var input = constructRequest(year, UUID.randomUUID().toString(), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Year")));
    }

    @ParameterizedTest(name = "identifier \"{0}\" is invalid")
    @ValueSource(strings = {" ", "abcd", "ab78ab78ab78ab78ab78a7ba87b8a7ba87b8"})
    void shouldReturnBadRequestWhenPathParameterIdentifierIsNotValid(String identifier) throws IOException {

        var input = constructRequest(String.valueOf(randomYear()), identifier, MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Pid")));
    }

    @Test
    void shouldReturnNotFoundWhenExternalApiRespondsWithNotFound() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = String.valueOf(randomYear());

        mockResponseWithHttpStatus(CHANNEL_REGISTRY_PATH_ELEMENT, identifier, year, HttpURLConnection.HTTP_NOT_FOUND);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_NOT_FOUND)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Publication channel not found!")));
    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCode() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = String.valueOf(randomYear());

        mockResponseWithHttpStatus(CHANNEL_REGISTRY_PATH_ELEMENT,
                                   identifier,
                                   year,
                                   HttpURLConnection.HTTP_INTERNAL_ERROR);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Error fetching publication channel"));
        assertThat(appender.getMessages(), containsString("500"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldLogErrorAndReturnBadGatewayWhenInterruptionOccurs() throws IOException, InterruptedException {
        ChannelRegistryClient publicationChannelClient = setupInterruptedClient();

        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment, publicationChannelClient);

        var input = constructRequest(String.valueOf(randomYear()), UUID.randomUUID().toString(), MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Unable to reach upstream!"));
        assertThat(appender.getMessages(), containsString(InterruptedException.class.getSimpleName()));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(equalTo("Unable to reach upstream!")));
    }

    @Test
    void shouldReturnRedirectWhenChannelRegistryReturnsRedirect() throws IOException {
        var year = String.valueOf(randomYear());
        var requestedIdentifier = UUID.randomUUID().toString();
        var newIdentifier = UUID.randomUUID().toString();
        var newChannelRegistryLocation = UriWrapper.fromHost(channelRegistryBaseUri)
                                             .addChild(CHANNEL_REGISTRY_PATH_ELEMENT, newIdentifier, year)
                                             .toString();
        mockRedirectedClient(requestedIdentifier, newChannelRegistryLocation, year, CHANNEL_REGISTRY_PATH_ELEMENT);
        handlerUnderTest.handleRequest(constructRequest(year, requestedIdentifier, MediaType.ANY_TYPE),
                                       output,
                                       context);
        var response = GatewayResponse.fromOutputStream(output, HttpResponse.class);
        assertEquals(HttpURLConnection.HTTP_MOVED_PERM, response.getStatusCode());
        var expectedLocation = createPublicationChannelUri(newIdentifier, PUBLISHER_PATH, year).toString();
        assertEquals(expectedLocation, response.getHeaders().get(LOCATION));
        assertEquals(WILD_CARD, response.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private static Stream<String> invalidYearsProvider() {
        var yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", yearAfterNextYear, "21000");
    }

    private PublisherDto mockPublisherFound(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier);
        var body = testChannel.asChannelRegistryPublisherBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return testChannel.asPublisherDto(SELF_URI_BASE, String.valueOf(year));
    }

    private PublisherDto mockPublisherFoundYearValueNull(String year, String identifier) {
        var testChannel = new TestChannel(null, identifier);
        var body = testChannel.asChannelRegistryPublisherBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return testChannel.asPublisherDto(SELF_URI_BASE, String.valueOf(year));
    }
}
