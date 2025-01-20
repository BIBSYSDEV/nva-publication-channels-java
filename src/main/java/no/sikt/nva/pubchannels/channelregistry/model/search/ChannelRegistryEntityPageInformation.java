package no.sikt.nva.pubchannels.channelregistry.model.search;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.sikt.nva.pubchannels.handler.search.ThirdPartyPageInformation;

@JsonSerialize
public record ChannelRegistryEntityPageInformation(Integer totalResults)
    implements ThirdPartyPageInformation {

}
