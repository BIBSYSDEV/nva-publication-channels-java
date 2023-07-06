package no.sikt.nva.pubchannels.dataporten;

import no.sikt.nva.pubchannels.dataporten.model.DataportenJournal;
import no.sikt.nva.pubchannels.dataporten.model.DataportenPublisher;
import no.sikt.nva.pubchannels.dataporten.model.DataportenSeries;
import no.sikt.nva.pubchannels.dataporten.model.search.DataportenSearchJournalResponse;
import no.sikt.nva.pubchannels.dataporten.model.search.DataportenSearchPublisherResponse;
import no.sikt.nva.pubchannels.dataporten.model.search.DataportenSearchSeriesResponse;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public enum ChannelType {

    JOURNAL("findjournal", DataportenJournal.class, DataportenSearchJournalResponse.class),
    PUBLISHER("findpublisher", DataportenPublisher.class, DataportenSearchPublisherResponse.class),
    SERIES("findseries", DataportenSeries.class, DataportenSearchSeriesResponse.class);

    public final String pathElement;
    public final Class<? extends ThirdPartyPublicationChannel> fetchResponseClass;

    public final Class<? extends ThirdPartySearchResponse> searchResponseClass;

    ChannelType(String pathElement, Class<? extends ThirdPartyPublicationChannel> fetchResponseClass,
                Class<? extends ThirdPartySearchResponse> searchResponseClass) {
        this.pathElement = pathElement;
        this.fetchResponseClass = fetchResponseClass;
        this.searchResponseClass = searchResponseClass;
    }
}
