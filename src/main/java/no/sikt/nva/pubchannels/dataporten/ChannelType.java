package no.sikt.nva.pubchannels.dataporten;

import no.sikt.nva.pubchannels.dataporten.fetch.FetchJournalByIdAndYearResponse;
import no.sikt.nva.pubchannels.dataporten.fetch.FetchSeriesByIdAndYearResponse;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublicationChannel;

public enum ChannelType {

    JOURNAL("findjournal", FetchJournalByIdAndYearResponse.class),
    PUBLISHER("findpublisher", null),
    SERIES("findseries", FetchSeriesByIdAndYearResponse.class);

    public final String pathElement;
    public final Class<? extends ThirdPartyPublicationChannel> responseClass;

    ChannelType(String pathElement, Class<? extends ThirdPartyPublicationChannel> responseClass) {
        this.pathElement = pathElement;
        this.responseClass = responseClass;
    }
}
