package no.sikt.nva.pubchannels.handler.fetch.journal;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
import static no.sikt.nva.pubchannels.TestConstants.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.LOCATION;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockChannelRegistryResponse;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.TestUtils;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.fetch.FetchSerialPublicationByIdentifierAndYearHandlerTest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class FetchJournalByIdentifierAndYearHandlerTest extends FetchSerialPublicationByIdentifierAndYearHandlerTest {

    private static final String JOURNAL_IDENTIFIER_FROM_CACHE = "50561B90-6679-4FCD-BCB0-99E521B18962";
    private static final String JOURNAL_YEAR_FROM_CACHE = "2024";
    private static final String CHANNEL_REGISTRY_PATH_ELEMENT = "/findjournal/";
    private static final URI SELF_URI_BASE = URI.create("https://localhost/publication-channels/" + JOURNAL_PATH);

    private PublicationChannelMockClient mockRegistry;

    @Override
    protected String getChannelRegistryPathParameter() {
        return CHANNEL_REGISTRY_PATH_ELEMENT;
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> createHandler(
        ChannelRegistryClient publicationChannelClient) {
        return new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                          super.getAppConfigWithCacheEnabled(false));
    }

    @Override
    protected SerialPublicationDto mockChannelFound(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Journal");
        var body = testChannel.asChannelRegistryJournalBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    @Override
    protected SerialPublicationDto mockChannelFoundYearValueNull(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Journal")
                              .withScientificValueReviewNotice(Map.of("en", "This is a review notice",
                                                                      "no", "Vedtak"));
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    @Override
    protected SerialPublicationDto mockChannelWithScientificValueReviewNotice(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Journal")
                              .withScientificValueReviewNotice(Map.of("en", "This is a review notice",
                                                                      "no", "Vedtak"));
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    @Override
    protected void mockChannelFound(int year, String identifier, String channelRegistryResponseBody) {
        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    channelRegistryResponseBody);
    }

    @Override
    protected TestChannel generateTestChannel(int year, String identifier) {
        return new TestChannel(year, identifier, "Journal");
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment,
                                                                           this.channelRegistryClient,
                                                                           this.cacheService,
                                                                           super.getAppConfigWithCacheEnabled(false));
        this.mockRegistry = new PublicationChannelMockClient();
    }

    @Test
    void shouldReturnBadGatewayWhenChannelRegistryIsUnavailableAndChannelIsNotCached() throws IOException {
        var httpClient = WiremockHttpClient.create();
        var channelRegistryBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource,
                                                                           cacheService,
                                                                           super.getAppConfigWithCacheEnabled(false));

        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        var input = constructRequest(year, identifier);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        var upstreamUrl = "https://localhost:9898" + CHANNEL_REGISTRY_PATH_ELEMENT + identifier + "/" + year;
        assertThat(appender.getMessages(), containsString("Unable to reach upstream: " + upstreamUrl));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unable to reach upstream!")));
    }

    @Test
    void shouldReturnRedirectWhenChannelRegistryReturnsRedirect() throws IOException {
        var year = randomYear();
        var requestedIdentifier = UUID.randomUUID().toString();
        var newIdentifier = UUID.randomUUID().toString();
        var newChannelRegistryLocation = UriWrapper.fromHost(channelRegistryBaseUri)
                                             .addChild(CHANNEL_REGISTRY_PATH_ELEMENT, newIdentifier, year)
                                             .toString();
        mockRegistry.redirect(requestedIdentifier, newChannelRegistryLocation, year);
        handlerUnderTest.handleRequest(constructRequest(year, requestedIdentifier), output, context);
        var response = GatewayResponse.fromOutputStream(output, HttpResponse.class);
        assertEquals(HTTP_MOVED_PERM, response.getStatusCode());
        var expectedLocation = constructExpectedLocation(newIdentifier, year);
        assertEquals(expectedLocation, response.getHeaders().get(LOCATION));
        assertEquals(WILD_CARD, response.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void shouldReturnJournalWhenChannelRegistryIsUnavailableAndJournalIsCached() throws IOException {
        var httpClient = WiremockHttpClient.create();
        var channelRegistryBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);
        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource,
                                                                           cacheService,
                                                                           super.getAppConfigWithCacheEnabled(true));

        var input = constructRequest(JOURNAL_YEAR_FROM_CACHE, JOURNAL_IDENTIFIER_FROM_CACHE);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(),
                   containsString("Fetching JOURNAL from cache: " + JOURNAL_IDENTIFIER_FROM_CACHE));

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_OK)));
    }

    @Test
    void shouldReturnJournalFromCacheWhenShouldUseCacheEnvironmentIsTrue() throws IOException {
        var httpClient = WiremockHttpClient.create();
        var channelRegistryBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);
        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource,
                                                                           cacheService,
                                                                           super.getAppConfigWithCacheEnabled(true));

        var input = constructRequest(JOURNAL_YEAR_FROM_CACHE, JOURNAL_IDENTIFIER_FROM_CACHE);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(),
                   containsString("Fetching JOURNAL from cache: " + JOURNAL_IDENTIFIER_FROM_CACHE));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_OK)));
    }

    @Test
    void shouldReturnNotFoundWhenShouldUseCacheEnvironmentVariableIsTrueButJournalIsNotCached() throws IOException {
        var httpClient = WiremockHttpClient.create();
        var channelRegistryBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);
        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource,
                                                                           cacheService,
                                                                           super.getAppConfigWithCacheEnabled(true));

        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        var input = constructRequest(year, identifier);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Could not find cached publication channel with"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_NOT_FOUND)));
    }

    private static String constructExpectedLocation(String newIdentifier, String year) {
        return UriWrapper.fromHost(API_DOMAIN)
                   .addChild(CUSTOM_DOMAIN_BASE_PATH, JOURNAL_PATH, newIdentifier, year)
                   .toString();
    }

    private static InputStream constructRequest(String year, String identifier, MediaType mediaType)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper).withHeaders(Map.of(ACCEPT, mediaType.toString()))
                   .withPathParameters(Map.of("identifier",
                                              identifier,
                                              "year",
                                              year))
                   .build();
    }

    private static InputStream constructRequest(String year, String identifier) throws JsonProcessingException {
        return constructRequest(year, identifier, MediaType.JSON_UTF_8);
    }

    private String randomYear() {
        var bound = (LocalDate.now().getYear() + 1) - YEAR_START;
        return Integer.toString(YEAR_START + randomInteger(bound));
    }
}
