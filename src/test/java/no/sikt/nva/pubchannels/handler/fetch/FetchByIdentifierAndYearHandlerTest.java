package no.sikt.nva.pubchannels.handler.fetch;

import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheServiceTestSetup;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

@WireMockTest(httpsEnabled = true)
public abstract class FetchByIdentifierAndYearHandlerTest extends CacheServiceTestSetup {

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
        super.setupDynamoDbTable();
        channelRegistryBaseUri = runtimeInfo.getHttpsBaseUrl();
        HttpClient httpClient = WiremockHttpClient.create();
        channelRegistryClient = new ChannelRegistryClient(httpClient, URI.create(channelRegistryBaseUri), null);
        cacheService = new CacheService(super.getClient());
        output = new ByteArrayOutputStream();
    }
}
