package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.LOCATION;
import static no.sikt.nva.pubchannels.TestConstants.PUBLISHER_PATH;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockChannelRegistryResponse;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockRedirectedClient;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockResponseWithHttpStatus;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandlerTest;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.zalando.problem.Problem;

class FetchPublisherByIdentifierAndYearHandlerTest extends FetchByIdentifierAndYearHandlerTest {

    private static final String PUBLISHER_IDENTIFIER_FROM_CACHE = "09D6F92E-B0F6-4B62-90AB-1B9E767E9E11";
    private static final URI SELF_URI_BASE = UriWrapper.fromHost(API_DOMAIN)
                                                       .addChild(CUSTOM_DOMAIN_BASE_PATH)
                                                       .addChild(PUBLISHER_PATH)
                                                       .getUri();

    @Override
    protected FetchByIdentifierAndYearHandler<Void, ?> createHandler(ChannelRegistryClient publicationChannelClient) {
        return new FetchPublisherByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                            super.getAppConfigWithCacheEnabled(false));
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment,
                                                                             this.channelRegistryClient,
                                                                             cacheService,
                                                                             super.getAppConfigWithCacheEnabled(false));
        this.customChannelPath = PUBLISHER_PATH;
        this.channelRegistryPathElement = "/findpublisher/";
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var expectedPublisher = mockPublisherFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualPublisher = response.getBodyObject(PublisherDto.class);
        assertThat(actualPublisher, is(equalTo(expectedPublisher)));
    }

    @Test
    void shouldIncludeYearInResponse() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);
        mockPublisherFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);
        var actualYear = response.getBodyObject(PublisherDto.class).year();
        assertThat(actualYear, is(equalTo(year)));
    }

    @Test
    void shouldNotFailWhenChannelRegistryLevelNull() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, identifier, PublisherDto.TYPE);
        mockPublisherFound(year, identifier, testChannel.asChannelRegistryPublisherBodyWithoutLevel());

        handlerUnderTest.handleRequest(constructRequest(year, identifier, MediaType.ANY_TYPE), output,
                                       context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);
        assertEquals(HTTP_OK, response.getStatusCode());
    }

    @Test
    void shouldIncludeScientificReviewNoticeWhenLevelDisplayX() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);
        var expectedPublisher = mockPublisherWithScientificValueReviewNotice(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);
        var actualReviewNotice = response.getBodyObject(PublisherDto.class).reviewNotice();
        assertThat(actualReviewNotice, is(equalTo(expectedPublisher.reviewNotice())));
    }

    @Test
    void shouldNotIncludeScientificReviewNoticeWhenLevelDisplayNotX() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);
        var actualPublisher = response.getBodyObject(PublisherDto.class);
        assertNull(actualPublisher.reviewNotice());
    }

    @ParameterizedTest(name = "Should return requested media type \"{0}\"")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType) throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier, mediaType);

        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedPublisher = mockPublisherFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);

        var actualPublisher = response.getBodyObject(PublisherDto.class);
        assertThat(actualPublisher, is(equalTo(expectedPublisher)));
        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));
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
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualPublisher = response.getBodyObject(PublisherDto.class);
        assertThat(actualPublisher, is(equalTo(expectedPublisher)));
    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCode() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        mockResponseWithHttpStatus("/findpublisher/",
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
    void shouldReturnRedirectWhenChannelRegistryReturnsRedirect() throws IOException {
        var year = randomYear();
        var requestedIdentifier = UUID.randomUUID().toString();
        var newIdentifier = UUID.randomUUID().toString();
        var newChannelRegistryLocation = UriWrapper.fromHost(channelRegistryBaseUri)
                                             .addChild("/findpublisher/", newIdentifier, year)
                                             .toString();
        mockRedirectedClient(requestedIdentifier, newChannelRegistryLocation, year, "/findpublisher/");
        handlerUnderTest.handleRequest(constructRequest(year, requestedIdentifier, MediaType.ANY_TYPE),
                                       output,
                                       context);
        var response = GatewayResponse.fromOutputStream(output, HttpResponse.class);
        assertEquals(HttpURLConnection.HTTP_MOVED_PERM, response.getStatusCode());
        var expectedLocation = createPublicationChannelUri(newIdentifier, PUBLISHER_PATH, year).toString();
        assertEquals(expectedLocation, response.getHeaders().get(LOCATION));
        assertEquals(WILD_CARD, response.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void shouldReturnPublisherWhenInterruptionOccursAndPublisherIsCached() throws IOException {
        var httpClient = WiremockHttpClient.create();
        var channelRegistryBaseUri = URI.create("https://localhost:9898");
        var channelRegistryClient = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);

        var publisherIdentifier = PUBLISHER_IDENTIFIER_FROM_CACHE;
        var input = constructRequest(randomYear(), publisherIdentifier, MediaType.ANY_TYPE);

        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment, channelRegistryClient,
                                                                             cacheService,
                                                                             super.getAppConfigWithCacheEnabled(true));
        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_OK)));
        assertThat(appender.getMessages(), containsString("Fetching PUBLISHER from cache: " + publisherIdentifier));
    }

    @Test
    void shouldReturnPublisherFromCacheWhenShouldUseCacheEnvironmentIsTrue() throws IOException {
        var publisherIdentifier = PUBLISHER_IDENTIFIER_FROM_CACHE;

        var input = constructRequest(randomYear(), publisherIdentifier, MediaType.ANY_TYPE);
        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");

        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment, null,
                                                                             cacheService,
                                                                             super.getAppConfigWithCacheEnabled(true));
        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_OK)));
        assertThat(appender.getMessages(), not(containsString("Unable to reach upstream!")));
        assertThat(appender.getMessages(), containsString("Fetching PUBLISHER from cache: " + publisherIdentifier));
    }

    @Test
    void shouldReturnNotFoundWhenShouldUseCacheEnvironmentVariableIsTrueButPublisherIsNotCached() throws IOException {
        var input = constructRequest(randomYear(), UUID.randomUUID().toString(), MediaType.ANY_TYPE);
        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment, null,
                                                                             cacheService,
                                                                             super.getAppConfigWithCacheEnabled(true));

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PublisherDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_NOT_FOUND)));
    }

    private PublisherDto mockPublisherFound(String year, String identifier) {
        var testChannel = new TestChannel(year, identifier, PublisherDto.TYPE);
        var body = testChannel.asChannelRegistryPublisherBody();

        mockChannelRegistryResponse("/findpublisher/", String.valueOf(year), identifier,
                                    body);

        return testChannel.asPublisherDto(SELF_URI_BASE, String.valueOf(year));
    }

    private void mockPublisherFound(String year, String identifier, String channelRegistryPublisherBody) {
        mockChannelRegistryResponse("/findpublisher/", year, identifier, channelRegistryPublisherBody);
    }

    private PublisherDto mockPublisherWithScientificValueReviewNotice(String year, String identifier) {
        var testChannel = new TestChannel(year, identifier, PublisherDto.TYPE)
                              .withScientificValueReviewNotice(Map.of("en", "some comment",
                                                                      "no", "vedtak"));
        var body = testChannel.asChannelRegistryPublisherBody();

        mockChannelRegistryResponse("/findpublisher/", String.valueOf(year), identifier, body);

        return testChannel.asPublisherDto(SELF_URI_BASE, String.valueOf(year));
    }

    private PublisherDto mockPublisherFoundYearValueNull(String year, String identifier) {
        var testChannel = new TestChannel(null, identifier, PublisherDto.TYPE);
        var body = testChannel.asChannelRegistryPublisherBody();

        mockChannelRegistryResponse("/findpublisher/", String.valueOf(year), identifier, body);

        return testChannel.asPublisherDto(SELF_URI_BASE, String.valueOf(year));
    }
}
