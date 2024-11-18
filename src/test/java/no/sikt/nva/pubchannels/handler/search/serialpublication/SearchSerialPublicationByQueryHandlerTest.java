package no.sikt.nva.pubchannels.handler.search.serialpublication;

import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIAL_PUBLICATION_PATH;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandlerTest;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

@WireMockTest(httpsEnabled = true)
class SearchSerialPublicationByQueryHandlerTest extends SearchByQueryHandlerTest {

    private static final Context context = new FakeContext();
    private static final String SELF_URI_BASE = "https://localhost/publication-channels/" + SERIAL_PUBLICATION_PATH;

    @Override
    protected String getPath() {
        return ChannelType.SERIAL_PUBLICATION.pathElement;
    }

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {

        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);
        var channelRegistryBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);

        this.handlerUnderTest = new SearchSerialPublicationByQueryHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }
}
