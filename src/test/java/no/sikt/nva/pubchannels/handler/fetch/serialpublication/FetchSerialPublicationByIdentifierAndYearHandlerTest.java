package no.sikt.nva.pubchannels.handler.fetch.serialpublication;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.TestConstants.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.sikt.nva.pubchannels.TestConstants.LOCATION;
import static no.sikt.nva.pubchannels.TestConstants.SERIAL_PUBLICATION_PATH;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockChannelRegistryResponse;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockRedirectedClient;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockResponseWithHttpStatus;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.fetch.series.FetchSeriesByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

public class FetchSerialPublicationByIdentifierAndYearHandlerTest extends
                                                                  no.sikt.nva.pubchannels.handler.fetch.FetchSerialPublicationByIdentifierAndYearHandlerTest {

    private static final String SERIES_IDENTIFIER_FROM_CACHE = "50561B90-6679-4FCD-BCB0-99E521B18962";
    private static final String SERIES_YEAR_FROM_CACHE = "2024";
    private static final URI SELF_URI_BASE = URI.create(
        "https://localhost/publication-channels/" + SERIAL_PUBLICATION_PATH);
    private static final String CHANNEL_REGISTRY_PATH_ELEMENT = "/findjournalserie/";

    @Override
    protected String getChannelRegistryPathParameter() {
        return CHANNEL_REGISTRY_PATH_ELEMENT;
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> createHandler(
        ChannelRegistryClient publicationChannelClient) {
        return new FetchSerialPublicationByIdentifierAndYearHandler(environment, publicationChannelClient,
                                                                    cacheService,
                                                                    super.getAppConfigWithCacheEnabled(false));
    }

    @Override
    protected SerialPublicationDto mockChannelFound(int year, String identifier) {
        return mockSerialPublicationFound(year, identifier, "Series");
    }

    @Override
    protected SerialPublicationDto mockChannelFoundYearValueNull(int year, String identifier) {
        var testChannel = new TestChannel(null, identifier, "Series");

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    testChannel.asChannelRegistrySeriesBody());

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    @Override
    protected SerialPublicationDto mockChannelWithScientificValueReviewNotice(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Series")
                              .withScientificValueReviewNotice(Map.of("en", "This is a review notice",
                                                                      "no", "Vedtak"));
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchSerialPublicationByIdentifierAndYearHandler(environment,
                                                                                     this.channelRegistryClient,
                                                                                     this.cacheService,
                                                                                     super.getAppConfigWithCacheEnabled(
                                                                                         false));
    }

    @ParameterizedTest(name = "Should not fail when channel registry level null for type \"{0}\"")
    @ValueSource(strings = {"Series", "Journal"})
    void shouldNotFailWhenChannelRegistryLevelNull(String type) throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, identifier, type);
        String channelRegistrySeriesBody = testChannel.asChannelRegistrySeriesBodyWithoutLevel();
        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    channelRegistrySeriesBody);

        handlerUnderTest.handleRequest(constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE), output,
                                       context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertEquals(HTTP_OK, response.getStatusCode());
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
        var expectedLocation = createPublicationChannelUri(newIdentifier, SERIAL_PUBLICATION_PATH, year).toString();
        assertEquals(expectedLocation, response.getHeaders().get(LOCATION));
        assertEquals(WILD_CARD, response.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void shouldReturnSeriesWhenChannelRegistryIsUnavailableAndSeriesIsCached() throws IOException {
        var identifier = SERIES_IDENTIFIER_FROM_CACHE;
        var year = SERIES_YEAR_FROM_CACHE;

        mockResponseWithHttpStatus(CHANNEL_REGISTRY_PATH_ELEMENT, identifier, year, HTTP_INTERNAL_ERROR);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        super.loadAndEnableCache();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Fetching SERIAL_PUBLICATION from cache: " + identifier));

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

    private SerialPublicationDto mockSerialPublicationFound(int year, String identifier, String type) {
        var testChannel = new TestChannel(year, identifier, type);
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }
}
