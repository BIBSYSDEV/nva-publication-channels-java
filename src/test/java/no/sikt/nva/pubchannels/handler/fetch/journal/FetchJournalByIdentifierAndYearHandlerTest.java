package no.sikt.nva.pubchannels.handler.fetch.journal;

import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;

import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.fetch.BaseFetchSerialPublicationByIdentifierAndYearHandlerTest;
import no.sikt.nva.pubchannels.handler.fetch.FetchPublicationChannelHandler;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;

class FetchJournalByIdentifierAndYearHandlerTest
    extends BaseFetchSerialPublicationByIdentifierAndYearHandlerTest {

  @Override
  protected FetchPublicationChannelHandler createHandler(
      ChannelRegistryClient publicationChannelClient) {
    return new FetchPublicationChannelHandler(
        environment,
        publicationChannelClient,
        cacheService,
        super.getAppConfigWithCacheEnabled(false));
  }

  @Override
  protected FetchPublicationChannelHandler createHandler(
      Environment environment,
      PublicationChannelClient publicationChannelClient,
      CacheService cacheService,
      AppConfig appConfigWithCacheEnabled) {
    return new FetchPublicationChannelHandler(
        environment, publicationChannelClient, cacheService, appConfigWithCacheEnabled);
  }

  @BeforeEach
  void setup() {
    this.handlerUnderTest =
        new FetchPublicationChannelHandler(
            environment,
            this.channelRegistryClient,
            this.cacheService,
            super.getAppConfigWithCacheEnabled(false));
    this.type = JOURNAL_TYPE;
    this.nvaChannelPath = JOURNAL_PATH;
    this.channelRegistryPathElement = "/findjournal/";
    this.selfBaseUri =
        UriWrapper.fromHost(API_DOMAIN)
            .addChild(CUSTOM_DOMAIN_BASE_PATH)
            .addChild(JOURNAL_PATH)
            .getUri();
  }
}
