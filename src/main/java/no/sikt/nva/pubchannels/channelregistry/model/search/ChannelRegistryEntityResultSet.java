package no.sikt.nva.pubchannels.channelregistry.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import no.sikt.nva.pubchannels.handler.search.ThirdPartyResultSet;

@JsonSerialize
public record ChannelRegistryEntityResultSet<T>(@JsonProperty(PAGERESULT_FIELD) List<T> pageResult)
    implements ThirdPartyResultSet<T> {

    private static final String PAGERESULT_FIELD = "pageresult";
}
