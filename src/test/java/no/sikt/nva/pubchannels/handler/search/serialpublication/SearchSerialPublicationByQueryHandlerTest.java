package no.sikt.nva.pubchannels.handler.search.serialpublication;

import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.SERIAL_PUBLICATION_PATH;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.search.BaseSearchSerialPublicationByQueryHandlerTest;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;

class SearchSerialPublicationByQueryHandlerTest
    extends BaseSearchSerialPublicationByQueryHandlerTest {

    @BeforeEach
    void setup() {
        this.handlerUnderTest =
            new SearchSerialPublicationByQueryHandler(environment, publicationChannelClient);
        this.type = JOURNAL_TYPE;
        this.customChannelPath = ChannelType.SERIAL_PUBLICATION.pathElement;
        this.selfBaseUri =
            UriWrapper
                .fromHost(API_DOMAIN)
                .addChild(CUSTOM_DOMAIN_BASE_PATH)
                .addChild(SERIAL_PUBLICATION_PATH)
                .getUri();
    }
}
