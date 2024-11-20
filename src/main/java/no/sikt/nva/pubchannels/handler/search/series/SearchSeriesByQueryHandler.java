package no.sikt.nva.pubchannels.handler.search.series;

import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.model.SeriesDto;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;

import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.URI;

public class SearchSeriesByQueryHandler extends SearchByQueryHandler<SeriesDto> {

    private static final String PATH_ELEMENT = "series";
    private static final ChannelType CHANNEL_TYPE = ChannelType.SERIES;

    @JacocoGenerated
    public SearchSeriesByQueryHandler() {
        super(PATH_ELEMENT, CHANNEL_TYPE);
    }

    public SearchSeriesByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(environment, publicationChannelClient, PATH_ELEMENT, CHANNEL_TYPE);
    }

    @Override
    protected SeriesDto createResult(URI baseUri, ThirdPartyPublicationChannel entityResult, String requestedYear) {
        return SeriesDto.create(baseUri, (ThirdPartySerialPublication) entityResult, requestedYear);
    }
}
