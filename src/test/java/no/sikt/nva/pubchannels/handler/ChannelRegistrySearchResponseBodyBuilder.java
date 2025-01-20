package no.sikt.nva.pubchannels.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistryEntityPageInformation;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.attempt.Try;

public class ChannelRegistrySearchResponseBodyBuilder {

    private final Map<String, Object> bodyMap = new ConcurrentHashMap<>();

    public ChannelRegistrySearchResponseBodyBuilder() {
    }

    public ChannelRegistrySearchResponseBodyBuilder withEntityPageInformation(
        ChannelRegistryEntityPageInformation pageInformation) {
        bodyMap.put("entityPageInformationDto", pageInformation);
        return this;
    }

    public ChannelRegistrySearchResponseBodyBuilder withEntityResultSet(ObjectNode entityResultSet) {
        bodyMap.put("entityResultSetDto", entityResultSet);
        return this;
    }

    public String build() {
        return Try
                   .attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(bodyMap))
                   .orElseThrow();
    }
}
