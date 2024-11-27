package no.sikt.nva.pubchannels.handler.fetch.journal;

import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
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

class FetchJournalByIdentifierAndYearHandlerTest extends BaseFetchSerialPublicationByIdentifierAndYearHandlerTest {

    @Override
    protected FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> createHandler(
        ChannelRegistryClient publicationChannelClient) {
        return new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                          super.getAppConfigWithCacheEnabled(false));
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, ?> createHandler(Environment environment,
                                                                     PublicationChannelClient publicationChannelClient,
                                                                     CacheService cacheService,
                                                                     AppConfig appConfigWithCacheEnabled) {
        return new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                          appConfigWithCacheEnabled);
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment,
                                                                           this.channelRegistryClient,
                                                                           this.cacheService,
                                                                           super.getAppConfigWithCacheEnabled(false));
        this.type = JOURNAL_TYPE;
        this.customChannelPath = JOURNAL_PATH;
        this.channelRegistryPathElement = "/findjournal/";
        this.selfBaseUri = URI.create("https://localhost/publication-channels/" + JOURNAL_PATH);
    }
}
