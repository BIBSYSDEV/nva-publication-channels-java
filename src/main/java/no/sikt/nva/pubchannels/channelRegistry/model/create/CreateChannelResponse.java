package no.sikt.nva.pubchannels.channelRegistry.model.create;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record CreateChannelResponse(String pid) {

}
