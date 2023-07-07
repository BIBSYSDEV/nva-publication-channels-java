package no.sikt.nva.pubchannels.handler.search.journal;

import java.net.URI;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class SearchJournalByQueryHandler extends SearchByQueryHandler<JournalResult> {

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
    protected JournalResult createResult(URI baseUri, ThirdPartyPublicationChannel entityResult, String requestedYear) {
        return JournalResult.create(baseUri, (ThirdPartyJournal) entityResult, requestedYear);
    }
}
