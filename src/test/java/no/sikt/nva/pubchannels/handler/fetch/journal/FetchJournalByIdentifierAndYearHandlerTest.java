package no.sikt.nva.pubchannels.handler.fetch.journal;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.fetch.BaseFetchSerialPublicationByIdentifierAndYearHandlerTest;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class FetchJournalByIdentifierAndYearHandlerTest extends BaseFetchSerialPublicationByIdentifierAndYearHandlerTest {

    private static final String JOURNAL_IDENTIFIER_FROM_CACHE = "50561B90-6679-4FCD-BCB0-99E521B18962";
    private static final String JOURNAL_YEAR_FROM_CACHE = "2024";
    private static final String CHANNEL_REGISTRY_PATH_ELEMENT = "/findjournal/";
    private static final URI SELF_URI_BASE = URI.create("https://localhost/publication-channels/" + JOURNAL_PATH);
    private static final String JOURNAL_PATH_ELEMENT = "journal";

    @Override
    protected String getChannelRegistryPathElement() {
        return CHANNEL_REGISTRY_PATH_ELEMENT;
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> createHandler(
        ChannelRegistryClient publicationChannelClient) {
        return new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                          super.getAppConfigWithCacheEnabled(false));
    }

    @Override
    protected URI getSelfBaseUri() {
        return SELF_URI_BASE;
    }

    @Override
    protected String getPath() {
        return JOURNAL_PATH_ELEMENT;
    }

    @Override
    protected String getType() {
        return JOURNAL_TYPE;
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment,
                                                                           this.channelRegistryClient,
                                                                           this.cacheService,
                                                                           super.getAppConfigWithCacheEnabled(false));
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

        var input = constructRequest(JOURNAL_YEAR_FROM_CACHE, JOURNAL_IDENTIFIER_FROM_CACHE,
                                     MediaType.ANY_TYPE);

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

        var input = constructRequest(JOURNAL_YEAR_FROM_CACHE, JOURNAL_IDENTIFIER_FROM_CACHE,
                                     MediaType.ANY_TYPE);

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

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Could not find cached publication channel with"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_NOT_FOUND)));
    }
}
