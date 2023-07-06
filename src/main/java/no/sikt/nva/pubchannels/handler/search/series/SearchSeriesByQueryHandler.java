package no.sikt.nva.pubchannels.handler.search.series;

import java.net.URI;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class SearchSeriesByQueryHandler extends SearchByQueryHandler<SeriesResult> {

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
    protected SeriesResult createResult(URI baseUri, ThirdPartyPublicationChannel entityResult) {
        return SeriesResult.create(baseUri, (ThirdPartySeries) entityResult);
    }
}
