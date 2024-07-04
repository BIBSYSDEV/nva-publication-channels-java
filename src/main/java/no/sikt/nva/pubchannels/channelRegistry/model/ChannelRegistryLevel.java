package no.sikt.nva.pubchannels.channelRegistry.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record ChannelRegistryLevel(Integer year,
                                   String level) {

}
