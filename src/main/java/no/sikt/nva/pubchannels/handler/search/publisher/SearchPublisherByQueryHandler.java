package no.sikt.nva.pubchannels.handler.search.publisher;

import java.net.URI;
import no.sikt.nva.pubchannels.channelRegistry.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

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
    protected PublisherResult createResult(URI baseUri, ThirdPartyPublicationChannel entityResult,
                                           String requestedYear) {
        return PublisherResult.create(baseUri, (ThirdPartyPublisher) entityResult, requestedYear);
    }
}
