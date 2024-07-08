package no.sikt.nva.pubchannels.channelregistry.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public record ChannelRegistrySearchJournalResponse(
    @JsonProperty(ENTITY_PAGE_INFORMATION) ChannelRegistryEntityPageInformation pageInformation,
    @JsonProperty(ENTITY_RESULT_SET) ChannelRegistryEntityResultSet<ChannelRegistryJournal> resultSet)
    implements ThirdPartySearchResponse {

    private static final String ENTITY_RESULT_SET = "entityResultSetDto";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformationDto";
}
