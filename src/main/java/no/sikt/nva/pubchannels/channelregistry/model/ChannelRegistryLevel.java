package no.sikt.nva.pubchannels.channelregistry.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record ChannelRegistryLevel(Integer year,
                                   String level,
                                   String levelDisplay,
                                   @JsonProperty("vedtak") String decisionNO,
                                   String decision) {

}
