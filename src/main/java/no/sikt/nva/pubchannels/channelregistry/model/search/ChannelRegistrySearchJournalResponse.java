package no.sikt.nva.pubchannels.channelregistry.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySerialPublication;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public record ChannelRegistrySearchJournalResponse(
    @JsonProperty("entityPageInformationDto") ChannelRegistryEntityPageInformation pageInformation,
    @JsonProperty("entityResultSetDto")
        ChannelRegistryEntityResultSet<ChannelRegistrySerialPublication> resultSet)
    implements ThirdPartySearchResponse {}
