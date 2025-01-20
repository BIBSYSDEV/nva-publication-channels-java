package no.sikt.nva.pubchannels.channelregistry;

import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySerialPublication;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistrySearchJournalResponse;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistrySearchPublisherResponse;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistrySearchSerialPublicationResponse;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistrySearchSeriesResponse;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public enum ChannelType {
    JOURNAL(
        "findjournal",
        ChannelRegistrySerialPublication.class,
        ChannelRegistrySearchJournalResponse.class),
    PUBLISHER(
        "findpublisher",
        ChannelRegistryPublisher.class,
        ChannelRegistrySearchPublisherResponse.class),
    SERIES(
        "findseries",
        ChannelRegistrySerialPublication.class,
        ChannelRegistrySearchSeriesResponse.class),
    SERIAL_PUBLICATION(
        "findjournalserie",
        ChannelRegistrySerialPublication.class,
        ChannelRegistrySearchSerialPublicationResponse.class);

    public final String pathElement;
    public final Class<? extends ThirdPartyPublicationChannel> fetchResponseClass;
    public final Class<? extends ThirdPartySearchResponse> searchResponseClass;

    ChannelType(
        String pathElement,
        Class<? extends ThirdPartyPublicationChannel> fetchResponseClass,
        Class<? extends ThirdPartySearchResponse> searchResponseClass) {
        this.pathElement = pathElement;
        this.fetchResponseClass = fetchResponseClass;
        this.searchResponseClass = searchResponseClass;
    }
}
