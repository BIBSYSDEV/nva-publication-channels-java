package no.sikt.nva.pubchannels.handler.fetch.series;

import static no.sikt.nva.pubchannels.TestConstants.SERIES_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_TYPE;
import java.net.URI;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.fetch.BaseFetchSerialPublicationByIdentifierAndYearHandlerTest;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;

class FetchSeriesByIdentifierAndYearHandlerTest extends BaseFetchSerialPublicationByIdentifierAndYearHandlerTest {

    private static final URI SELF_URI_BASE = URI.create("https://localhost/publication-channels/" + SERIES_PATH);
    private static final String CHANNEL_REGISTRY_PATH_ELEMENT = "/findseries/";
    private static final String SERIES_PATH_ELEMENT = "series";

    @Override
    protected String getChannelRegistryPathElement() {
        return CHANNEL_REGISTRY_PATH_ELEMENT;
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> createHandler(
        ChannelRegistryClient publicationChannelClient) {
        return new FetchSeriesByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                         super.getAppConfigWithCacheEnabled(false));
    }

    @Override
    protected URI getSelfBaseUri() {
        return SELF_URI_BASE;
    }

    @Override
    protected String getPath() {
        return SERIES_PATH_ELEMENT;
    }

    @Override
    protected String getType() {
        return SERIES_TYPE;
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, ?> createHandler(Environment environment,
                                                                     PublicationChannelClient publicationChannelClient,
                                                                     CacheService cacheService,
                                                                     AppConfig appConfigWithCacheEnabled) {
        return new FetchSeriesByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                         appConfigWithCacheEnabled);
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment,
                                                                          this.channelRegistryClient,
                                                                          this.cacheService,
                                                                          super.getAppConfigWithCacheEnabled(false));
    }
}
