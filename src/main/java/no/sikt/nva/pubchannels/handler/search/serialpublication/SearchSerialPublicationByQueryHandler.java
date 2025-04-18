package no.sikt.nva.pubchannels.handler.search.serialpublication;

import java.net.URI;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandler;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class SearchSerialPublicationByQueryHandler
    extends SearchByQueryHandler<SerialPublicationDto> {

  private static final String PATH_ELEMENT = "serial-publication";
  private static final ChannelType CHANNEL_TYPE = ChannelType.SERIAL_PUBLICATION;

  @JacocoGenerated
  public SearchSerialPublicationByQueryHandler() {
    super(PATH_ELEMENT, CHANNEL_TYPE);
  }

  protected SearchSerialPublicationByQueryHandler(
      Environment environment, PublicationChannelClient publicationChannelClient) {
    super(environment, publicationChannelClient, PATH_ELEMENT, CHANNEL_TYPE);
  }

  @Override
  protected SerialPublicationDto createResult(
      URI baseUri, ThirdPartyPublicationChannel entityResult, String requestedYear) {
    return SerialPublicationDto.create(
        baseUri, (ThirdPartySerialPublication) entityResult, requestedYear);
  }
}
