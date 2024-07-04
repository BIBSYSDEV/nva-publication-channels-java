package no.sikt.nva.pubchannels.channelRegistry.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.sikt.nva.pubchannels.channelRegistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

@JsonSerialize
public record ChannelRegistrySearchPublisherResponse(
    @JsonProperty(ENTITY_PAGE_INFORMATION) ChannelRegistryEntityPageInformation pageInformation,
    @JsonProperty(ENTITY_RESULT_SET) ChannelRegistryEntityResultSet<ChannelRegistryPublisher> resultSet)
    implements ThirdPartySearchResponse {

    private static final String ENTITY_RESULT_SET = "entityResultSetDto";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformationDto";
}
