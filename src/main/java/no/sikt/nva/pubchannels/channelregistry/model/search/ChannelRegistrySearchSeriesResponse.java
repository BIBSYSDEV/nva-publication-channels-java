package no.sikt.nva.pubchannels.channelregistry.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySeries;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

@JsonSerialize
public record ChannelRegistrySearchSeriesResponse(
    @JsonProperty(ENTITY_PAGE_INFORMATION) ChannelRegistryEntityPageInformation pageInformation,
    @JsonProperty(ENTITY_RESULT_SET) ChannelRegistryEntityResultSet<ChannelRegistrySeries> resultSet)
    implements ThirdPartySearchResponse {

    private static final String ENTITY_RESULT_SET = "entityResultSetDto";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformationDto";
}
