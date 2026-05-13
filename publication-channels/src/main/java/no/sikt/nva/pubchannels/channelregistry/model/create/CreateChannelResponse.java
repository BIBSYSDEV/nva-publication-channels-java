package no.sikt.nva.pubchannels.channelregistry.model.create;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record CreateChannelResponse(String pid) {}
