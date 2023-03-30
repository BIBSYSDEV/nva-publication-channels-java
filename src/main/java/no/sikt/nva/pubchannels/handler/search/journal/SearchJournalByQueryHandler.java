package no.sikt.nva.pubchannels.handler.search.journal;

import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class SearchJournalByQueryHandler extends SearchByQueryHandler {

    private static final String JOURNAL_PATH_ELEMENT = "journal";

    @JacocoGenerated
    public SearchJournalByQueryHandler() {
        super(JOURNAL_PATH_ELEMENT, ChannelType.JOURNAL);
    }

    public SearchJournalByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(environment, publicationChannelClient, JOURNAL_PATH_ELEMENT, ChannelType.JOURNAL);
    }
}
