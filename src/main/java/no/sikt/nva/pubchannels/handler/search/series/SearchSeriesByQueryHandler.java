package no.sikt.nva.pubchannels.handler.search.series;

import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.dataporten.search.DataportenEntityResult;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.URI;

public class SearchSeriesByQueryHandler extends SearchByQueryHandler<SeriesResult> {

    private static final String PATH_ELEMENT = "series";
    private static final ChannelType CHANNEL_TYPE = ChannelType.SERIES;

    @JacocoGenerated
    protected SearchSeriesByQueryHandler() {
        super(PATH_ELEMENT, CHANNEL_TYPE);
    }

    public SearchSeriesByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(environment, publicationChannelClient, PATH_ELEMENT, CHANNEL_TYPE);
    }

    @Override
    protected SeriesResult createResult(URI baseUri, DataportenEntityResult entityResult) {
        return SeriesResult.create(baseUri, entityResult);
    }
}
