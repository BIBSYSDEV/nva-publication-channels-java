package no.sikt.nva.pubchannels.channelregistry;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public record UpdatePublisherRequest(String name, String isbn) implements UpdateChannelRequest {

  @Override
  public ChannelRegistryUpdateChannelRequest toChannelRegistryUpdateRequest(UUID identifier) {
    return new ChannelRegistryUpdateChannelRequest(identifier, name, null, null, isbn, "publisher");
  }
}
