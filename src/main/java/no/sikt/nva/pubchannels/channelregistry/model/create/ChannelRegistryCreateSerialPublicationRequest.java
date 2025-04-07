package no.sikt.nva.pubchannels.channelregistry.model.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;

public record ChannelRegistryCreateSerialPublicationRequest(
    @JsonProperty("name") String name,
    @JsonProperty("pissn") String printIssn,
    @JsonProperty("eissn") String onlineIssn,
    @JsonProperty("url") String url) {

  public static ChannelRegistryCreateSerialPublicationRequest fromClientRequest(
      CreateSerialPublicationRequest request) {
    return new ChannelRegistryCreateSerialPublicationRequest(
        request.name(), request.printIssn(), request.onlineIssn(), request.homepage());
  }
}
