package no.sikt.nva.pubchannels.handler.search.series;

import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class SearchSeriesByQueryHandler extends SearchByQueryHandler {

    private static final String PATH_ELEMENT = "series";

    @JacocoGenerated
    protected SearchSeriesByQueryHandler() {
        super(PATH_ELEMENT, ChannelType.SERIES);
    }

    public SearchSeriesByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(environment, publicationChannelClient, PATH_ELEMENT, ChannelType.SERIES);
    }
}
