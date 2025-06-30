package no.sikt.nva.pubchannels.channelregistry;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import no.unit.nva.commons.json.JsonSerializable;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(UpdatePublisherRequest.class),
  @JsonSubTypes.Type(UpdateSerialPublicationRequest.class),
})
public sealed interface UpdateChannelRequest extends JsonSerializable
    permits UpdatePublisherRequest, UpdateSerialPublicationRequest {

  ChannelRegistryUpdateChannelRequest toChannelRegistryUpdateRequest(String identifier);

  boolean isEmpty();
}
