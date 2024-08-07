package no.sikt.nva.pubchannels.channelregistry;

import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySeries;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistrySearchJournalResponse;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistrySearchPublisherResponse;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistrySearchSeriesResponse;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public enum ChannelType {

    JOURNAL("findjournal", ChannelRegistryJournal.class, ChannelRegistrySearchJournalResponse.class),
    PUBLISHER("findpublisher", ChannelRegistryPublisher.class, ChannelRegistrySearchPublisherResponse.class),
    SERIES("findseries", ChannelRegistrySeries.class, ChannelRegistrySearchSeriesResponse.class);

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
