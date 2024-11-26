package no.sikt.nva.pubchannels.handler.fetch.series;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.sikt.nva.pubchannels.TestConstants.LOCATION;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_PATH;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublicationChannelUri;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandlerTest;
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

class FetchSeriesByIdentifierAndYearHandlerTest extends FetchByIdentifierAndYearHandlerTest {

    private static final String SERIES_IDENTIFIER_FROM_CACHE = "50561B90-6679-4FCD-BCB0-99E521B18962";
    private static final String SERIES_YEAR_FROM_CACHE = "2024";
    private static final URI SELF_URI_BASE = URI.create("https://localhost/publication-channels/" + SERIES_PATH);
    private static final String CHANNEL_REGISTRY_PATH_ELEMENT = "/findseries/";

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment,
                                                                          this.channelRegistryClient,
                                                                          this.cacheService,
                                                                          super.getAppConfigWithCacheEnabled(false));
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);

        var expectedSeries = mockSeriesFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
    }

    @Test
    void shouldIncludeYearInResponse() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);
        mockSeriesFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualYear = response.getBodyObject(SerialPublicationDto.class).year();
        assertThat(actualYear, is(equalTo(String.valueOf(year))));
    }

    @ParameterizedTest(name = "Should return requested media type \"{0}\"")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType) throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, mediaType);

        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedSeries = mockSeriesFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));
        var contentType = response.getHeaders().get(CONTENT_TYPE);
        assertThat(contentType, is(equalTo(expectedMediaType)));
    }

    @Test
    void shouldReturnChannelIdWithRequestedYearIfThirdPartyDoesNotProvideYear() throws IOException {
        var year = String.valueOf(randomYear());
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var expectedSeries = mockSeriesFoundYearValueNull(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
    }

    @Test
    void shouldIncludeScientificReviewNoticeWhenLevelDisplayX() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);
        var expectedSeries = mockSeriesWithScientificValueReviewNotice(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualReviewNotice = response.getBodyObject(SerialPublicationDto.class).reviewNotice();
        assertThat(actualReviewNotice, is(equalTo(expectedSeries.reviewNotice())));
    }

    @Test
    void shouldNotIncludeScientificReviewNoticeWhenLevelDisplayNotX() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertNull(actualSeries.reviewNotice());
    }

    @Test
    void shouldNotFailWhenChannelRegistryLevelNull() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, identifier, "Series");
        mockSeriesFound(year, identifier, testChannel.asChannelRegistrySeriesBodyWithoutLevel());

        handlerUnderTest.handleRequest(constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE), output,
                                       context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertEquals(HTTP_OK, response.getStatusCode());
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#invalidYearsProvider")
    void shouldReturnBadRequestWhenPathParameterYearIsNotValid(String year) throws IOException {

        var input = constructRequest(year, UUID.randomUUID().toString(), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Year")));
    }

    @ParameterizedTest(name = "identifier \"{0}\" is invalid")
    @ValueSource(strings = {" ", "abcd", "ab78ab78ab78ab78ab78a7ba87b8a7ba87b8"})
    void shouldReturnBadRequestWhenPathParameterIdentifierIsNotValid(String identifier) throws IOException {

        var input = constructRequest(String.valueOf(randomYear()), identifier, MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Pid")));
    }

    @Test
    void shouldReturnNotFoundWhenExternalApiRespondsWithNotFound() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = String.valueOf(randomYear());

        mockResponseWithHttpStatus("/findseries/", identifier, year, HTTP_NOT_FOUND);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_NOT_FOUND)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Publication channel not found!")));
    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCodeAndSeriesIsNotCached()
        throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = String.valueOf(randomYear());

        mockResponseWithHttpStatus("/findseries/", identifier, year, HTTP_INTERNAL_ERROR);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Error fetching publication channel"));
        assertThat(appender.getMessages(), containsString("500"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldLogErrorAndReturnBadGatewayWhenInterruptionOccurs() throws IOException, InterruptedException {
        ChannelRegistryClient publicationChannelClient = setupInterruptedClient();

        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment, publicationChannelClient,
                                                                          cacheService,
                                                                          super.getAppConfigWithCacheEnabled(false));

        var input = constructRequest(String.valueOf(randomYear()), UUID.randomUUID().toString(), MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Unable to reach upstream!"));
        assertThat(appender.getMessages(), containsString(InterruptedException.class.getSimpleName()));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_GATEWAY)));

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
        assertEquals(HTTP_MOVED_PERM, response.getStatusCode());
        var expectedLocation = createPublicationChannelUri(newIdentifier, SERIES_PATH, year).toString();
        assertEquals(expectedLocation, response.getHeaders().get(LOCATION));
        assertEquals(WILD_CARD, response.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void shouldReturnSeriesWhenChannelRegistryIsUnavailableAndSeriesIsCached() throws IOException {
        var identifier = SERIES_IDENTIFIER_FROM_CACHE;
        var year = SERIES_YEAR_FROM_CACHE;

        mockResponseWithHttpStatus("/findseries/", identifier, year, HTTP_INTERNAL_ERROR);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        super.loadAndEnableCache();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Fetching SERIES from cache: " + identifier));

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_OK)));
    }

    @Test
    void shouldReturnSeriesFromCacheWhenShouldUseCacheEnvironmentVariableIsTrue() throws IOException {

        var input = constructRequest(SERIES_YEAR_FROM_CACHE, SERIES_IDENTIFIER_FROM_CACHE, MediaType.ANY_TYPE);

        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment, null, cacheService,
                                                                          super.getAppConfigWithCacheEnabled(true));
        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertThat(appender.getMessages(),
                   containsString("Fetching SERIES from cache: " + SERIES_IDENTIFIER_FROM_CACHE));

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));
    }

    @Test
    void shouldReturnNotFoundWhenShouldUseCacheEnvironmentVariableIsTrueButSeriesIsNotCached() throws IOException {
        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment, null, cacheService,
                                                                          super.getAppConfigWithCacheEnabled(true));

        var identifier = UUID.randomUUID().toString();
        var year = String.valueOf(randomYear());

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Could not find cached publication channel with"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_NOT_FOUND)));
    }

    private SerialPublicationDto mockSeriesFound(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Series");
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    private void mockSeriesFound(int year, String identifier, String channelRegistrySeriesBody) {
        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    channelRegistrySeriesBody);
    }

    private SerialPublicationDto mockSeriesWithScientificValueReviewNotice(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Series")
                              .withScientificValueReviewNotice(Map.of("en", "This is a review notice",
                                                                      "no", "Vedtak"));
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    private SerialPublicationDto mockSeriesFoundYearValueNull(String year, String identifier) {
        var testChannel = new TestChannel(null, identifier, "Series");

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, year, identifier,
                                    testChannel.asChannelRegistrySeriesBody());

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, year);
    }
}
