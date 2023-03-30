package no.sikt.nva.pubchannels.handler.search.publisher;

import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class SearchPublisherByQueryHandler extends SearchByQueryHandler {

    private static final String PUBLISHER_PATH_ELEMENT = "publisher";

    @JacocoGenerated
    public SearchPublisherByQueryHandler() {
        super(PUBLISHER_PATH_ELEMENT, ChannelType.PUBLISHER);
    }

    public SearchPublisherByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(environment, publicationChannelClient, PUBLISHER_PATH_ELEMENT, ChannelType.PUBLISHER);
    }
}
