package no.sikt.nva.pubchannels.channelregistry;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public record UpdateSerialPublicationRequest(String name, String printIssn, String onlineIssn)
    implements UpdateChannelRequest {

  @Override
  public ChannelRegistryUpdateChannelRequest toChannelRegistryUpdateRequest(UUID identifier) {
    return new ChannelRegistryUpdateChannelRequest(
        identifier, name, printIssn, onlineIssn, null, "serial-publication");
  }
}
