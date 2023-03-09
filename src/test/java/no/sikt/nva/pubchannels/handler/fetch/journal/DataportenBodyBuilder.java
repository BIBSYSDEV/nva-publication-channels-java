package no.sikt.nva.pubchannels.handler.fetch.journal;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.attempt.Try;

public class DataportenBodyBuilder {
    private final Map<String, Object> bodyMap = new ConcurrentHashMap<>();

    public DataportenBodyBuilder() {
    }

    public DataportenBodyBuilder withType(String type) {
        bodyMap.put("type", type);
        return this;
    }

    public DataportenBodyBuilder withPid(String pid) {
        bodyMap.put("pid", pid);
        return this;
    }

    public DataportenBodyBuilder withName(String name) {
        bodyMap.put("name", name);
        return this;
    }

    public DataportenBodyBuilder withEissn(String eissn) {
        bodyMap.put("eissn", eissn);
        return this;
    }

    public DataportenBodyBuilder withPissn(String pissn) {
        bodyMap.put("pissn", pissn);
        return this;
    }

    public DataportenBodyBuilder withYear(String year) {
        bodyMap.put("year", year);
        return this;
    }

    public DataportenBodyBuilder withLevel(String level) {
        if (Objects.nonNull(level)) {
            bodyMap.put("level", level);
        }
        return this;
    }

    public DataportenBodyBuilder withKurl(String kurl) {
        bodyMap.put("kURL", kurl);
        return this;
    }

    public String build() {
        return Try.attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(bodyMap)).orElseThrow();
    }
}
