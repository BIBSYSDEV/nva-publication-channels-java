package no.sikt.nva.pubchannels.handler.search.publisher;

import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.dataporten.search.DataportenEntityResult;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.URI;

public class SearchPublisherByQueryHandler extends SearchByQueryHandler<PublisherResult> {

    private static final String PUBLISHER_PATH_ELEMENT = "publisher";
    private static final ChannelType CHANNEL_TYPE = ChannelType.PUBLISHER;

    @JacocoGenerated
    public SearchPublisherByQueryHandler() {
        super(PUBLISHER_PATH_ELEMENT, CHANNEL_TYPE);
    }

    public SearchPublisherByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(environment, publicationChannelClient, PUBLISHER_PATH_ELEMENT, CHANNEL_TYPE);
    }

    @Override
    protected PublisherResult createResult(URI baseUri, DataportenEntityResult entityResult) {
        return PublisherResult.create(baseUri, entityResult);
    }
}
