package no.sikt.nva.pubchannels.handler.search.journal;

import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.model.JournalDto;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;

import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.URI;

public class SearchJournalByQueryHandler extends SearchByQueryHandler<JournalDto> {

    private static final String PATH_ELEMENT = "journal";
    private static final ChannelType CHANNEL_TYPE = ChannelType.JOURNAL;

    @JacocoGenerated
    public SearchJournalByQueryHandler() {
        super(PATH_ELEMENT, CHANNEL_TYPE);
    }

    public SearchJournalByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(environment, publicationChannelClient, PATH_ELEMENT, CHANNEL_TYPE);
    }

    @Override
    protected JournalDto createResult(URI baseUri, ThirdPartyPublicationChannel entityResult, String requestedYear) {
        return JournalDto.create(
                baseUri, (ThirdPartySerialPublication) entityResult, requestedYear);
    }
}
