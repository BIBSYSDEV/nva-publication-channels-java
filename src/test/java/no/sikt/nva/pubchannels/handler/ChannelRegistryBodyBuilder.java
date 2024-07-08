package no.sikt.nva.pubchannels.handler;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistryEntityPageInformation;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.attempt.Try;

public class ChannelRegistryBodyBuilder {

    private final Map<String, Object> bodyMap = new ConcurrentHashMap<>();

    public ChannelRegistryBodyBuilder() {
    }

    public ChannelRegistryBodyBuilder withType(String type) {
        bodyMap.put("type", type);
        return this;
    }

    public ChannelRegistryBodyBuilder withPid(String pid) {
        bodyMap.put("pid", pid);
        return this;
    }

    public ChannelRegistryBodyBuilder withName(String name) {
        bodyMap.put("name", name);
        return this;
    }

    public ChannelRegistryBodyBuilder withOriginalTitle(String originalTitle) {
        bodyMap.put("originalTitle", originalTitle);
        return this;
    }

    public ChannelRegistryBodyBuilder withEissn(String eissn) {
        bodyMap.put("eissn", eissn);
        return this;
    }

    public ChannelRegistryBodyBuilder withPissn(String pissn) {
        bodyMap.put("pissn", pissn);
        return this;
    }

    public ChannelRegistryBodyBuilder withIsbnPrefix(String isbnPrefix) {
        bodyMap.put("isbnprefix", isbnPrefix);
        return this;
    }

    public ChannelRegistryBodyBuilder withLevel(ChannelRegistryLevel level) {
        if (nonNull(level)) {
            bodyMap.put("levelElementDto", level);
        }
        return this;
    }

    public ChannelRegistryBodyBuilder withCeased(String ceased) {
        bodyMap.put("ceased", ceased);
        return this;
    }

    public ChannelRegistryBodyBuilder withKurl(String kurl) {
        bodyMap.put("kurl", kurl);
        return this;
    }

    public ChannelRegistryBodyBuilder withEntityPageInformation(ChannelRegistryEntityPageInformation pageInformation) {
        bodyMap.put("entityPageInformationDto", pageInformation);
        return this;
    }

    public ChannelRegistryBodyBuilder withEntityResultSet(ObjectNode entityResultSet) {
        bodyMap.put("entityResultSetDto", entityResultSet);
        return this;
    }

    public String build() {
        return Try.attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(bodyMap)).orElseThrow();
    }
}
