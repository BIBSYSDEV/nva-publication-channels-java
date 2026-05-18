package no.sikt.nva.pubchannels.handler.search.series;

import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_TYPE;

import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.search.BaseSearchSerialPublicationByQueryHandlerTest;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;

class SearchSeriesByQueryHandlerTest extends BaseSearchSerialPublicationByQueryHandlerTest {

  @BeforeEach
  void setup() {
    this.handlerUnderTest = new SearchSeriesByQueryHandler(environment, publicationChannelClient);
    this.type = SERIES_TYPE;
    this.customChannelPath = ChannelType.SERIES.channelRegistryPathElement;
    this.selfBaseUri =
        UriWrapper.fromHost(API_DOMAIN)
            .addChild(CUSTOM_DOMAIN_BASE_PATH)
            .addChild(SERIES_PATH)
            .getUri();
  }
}
