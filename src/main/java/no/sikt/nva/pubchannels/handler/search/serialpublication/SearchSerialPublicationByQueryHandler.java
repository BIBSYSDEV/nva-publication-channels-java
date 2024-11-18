package no.sikt.nva.pubchannels.handler.search.serialpublication;

import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;

import nva.commons.core.JacocoGenerated;

import java.net.URI;

public class SearchSerialPublicationByQueryHandler
        extends SearchByQueryHandler<SerialPublicationDto> {

    private static final String PATH_ELEMENT = "serial-publication";
    private static final ChannelType CHANNEL_TYPE = ChannelType.SERIAL_PUBLICATION;

    @JacocoGenerated
    public SearchSerialPublicationByQueryHandler() {
        super(PATH_ELEMENT, CHANNEL_TYPE);
    }

    protected SearchSerialPublicationByQueryHandler(String pathElement, ChannelType channelType) {
        super(pathElement, channelType);
    }

    @Override
    protected SerialPublicationDto createResult(
            URI baseUri, ThirdPartyPublicationChannel entityResult, String requestedYear) {
        return null;
    }
}
