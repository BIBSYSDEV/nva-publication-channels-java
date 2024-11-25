package no.sikt.nva.pubchannels.handler.fetch;

import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheServiceTestSetup;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.core.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

@WireMockTest(httpsEnabled = true)
public abstract class FetchByIdentifierAndYearHandlerTest extends CacheServiceTestSetup {

    public static final String JOURNAL_IDENTIFIER_FROM_CACHE = "50561B90-6679-4FCD-BCB0-99E521B18962";
    public static final String JOURNAL_YEAR_FROM_CACHE = "2024";
    protected static final int YEAR_START = 2004;
    protected static final Context context = new FakeContext();
    protected static Environment environment;
    protected CacheService cacheService;
    protected ByteArrayOutputStream output;
    protected String channelRegistryBaseUri;
    protected ChannelRegistryClient channelRegistryClient;
    protected FetchByIdentifierAndYearHandler<Void, ?> handlerUnderTest;

    @BeforeAll
    public static void commonBeforeAll() {
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);
    }

    @BeforeEach
    void commonBeforeEach(WireMockRuntimeInfo runtimeInfo) {
        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("false");
        channelRegistryBaseUri = runtimeInfo.getHttpsBaseUrl();
        HttpClient httpClient = WiremockHttpClient.create();
        channelRegistryClient = new ChannelRegistryClient(httpClient, URI.create(channelRegistryBaseUri), null);
        cacheService = new CacheService(super.getClient());
        output = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        output.flush();
    }
}
