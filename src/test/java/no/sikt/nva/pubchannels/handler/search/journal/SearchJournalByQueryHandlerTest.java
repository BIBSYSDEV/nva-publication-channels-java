package no.sikt.nva.pubchannels.handler.search.journal;

import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;

import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.search.BaseSearchSerialPublicationByQueryHandlerTest;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;

class SearchJournalByQueryHandlerTest extends BaseSearchSerialPublicationByQueryHandlerTest {

  @BeforeEach
  void setup() {
    this.handlerUnderTest = new SearchJournalByQueryHandler(environment, publicationChannelClient);
    this.type = JOURNAL_TYPE;
    this.customChannelPath = ChannelType.JOURNAL.channelRegistryPathElement;
    this.selfBaseUri =
        UriWrapper.fromHost(API_DOMAIN)
            .addChild(CUSTOM_DOMAIN_BASE_PATH)
            .addChild(JOURNAL_PATH)
            .getUri();
  }
}
