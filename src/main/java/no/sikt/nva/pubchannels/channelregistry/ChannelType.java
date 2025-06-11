package no.sikt.nva.pubchannels.channelregistry;

import java.util.Locale;
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
      "journal",
      ChannelRegistrySerialPublication.class,
      ChannelRegistrySearchJournalResponse.class),
  PUBLISHER(
      "findpublisher",
      "publisher",
      ChannelRegistryPublisher.class,
      ChannelRegistrySearchPublisherResponse.class),
  SERIES(
      "findseries",
      "series",
      ChannelRegistrySerialPublication.class,
      ChannelRegistrySearchSeriesResponse.class),
  SERIAL_PUBLICATION(
      "findjournalserie",
      "serial-publication",
      ChannelRegistrySerialPublication.class,
      ChannelRegistrySearchSerialPublicationResponse.class);

  public final String channelRegistryPathElement;
  public final String nvaPathElement;
  public final Class<? extends ThirdPartyPublicationChannel> fetchResponseClass;
  public final Class<? extends ThirdPartySearchResponse> searchResponseClass;

  ChannelType(
      String pathElement,
      String nvaPathElement,
      Class<? extends ThirdPartyPublicationChannel> fetchResponseClass,
      Class<? extends ThirdPartySearchResponse> searchResponseClass) {
    this.channelRegistryPathElement = pathElement;
    this.nvaPathElement = nvaPathElement;
    this.fetchResponseClass = fetchResponseClass;
    this.searchResponseClass = searchResponseClass;
  }

  public static ChannelType fromNvaPathElement(String value) {
    return switch (value.toLowerCase(Locale.getDefault())) {
      case "publisher" -> PUBLISHER;
      case "serial-publication" -> SERIAL_PUBLICATION;
      case "journal" -> JOURNAL;
      case "series" -> SERIES;
      default ->
          throw new IllegalStateException(
              "Unexpected value: " + value.toLowerCase(Locale.getDefault()));
    };
  }

  public String getNvaPathElement() {
    return nvaPathElement;
  }

  public Class<? extends ThirdPartyPublicationChannel> getFetchResponseClass() {
    return fetchResponseClass;
  }

  public String getChannelRegistryPathElement() {
    return channelRegistryPathElement;
  }
}
