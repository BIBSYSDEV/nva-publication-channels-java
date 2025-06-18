package no.sikt.nva.pubchannels.channelregistry;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryUpdateChannelRequest.Fields;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public record UpdatePublisherRequest(String name, String isbn) implements UpdateChannelRequest {

  @Override
  public ChannelRegistryUpdateChannelRequest toChannelRegistryUpdateRequest(UUID identifier) {
    return new ChannelRegistryUpdateChannelRequest(
        new Fields(identifier, name, null, null, isbn), "publisher");
  }

  @Override
  public boolean isEmpty() {
    return isNull(name) && isNull(isbn);
  }
}
