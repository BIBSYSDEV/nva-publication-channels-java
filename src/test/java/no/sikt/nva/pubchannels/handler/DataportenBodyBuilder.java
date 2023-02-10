package no.sikt.nva.pubchannels.handler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.attempt.Try;

public class DataportenBodyBuilder {
    private final Map<String, Object> bodyMap = new ConcurrentHashMap<>();

    public DataportenBodyBuilder() {
        bodyMap.put("type", "Journal");
    }

    public DataportenBodyBuilder withPid(String pid) {
        bodyMap.put("Pid", pid);
        return this;
    }

    public DataportenBodyBuilder withName(String name) {
        bodyMap.put("Name", name);
        return this;
    }

    public DataportenBodyBuilder withEissn(String eissn) {
        bodyMap.put("Eissn", eissn);
        return this;
    }

    public DataportenBodyBuilder withPissn(String pissn) {
        bodyMap.put("Pissn", pissn);
        return this;
    }

    public DataportenBodyBuilder withYear(String year) {
        bodyMap.put("Year", year);
        return this;
    }

    public DataportenBodyBuilder withLevel(String level) {
        if (Objects.nonNull(level)) {
            bodyMap.put("Level", level);
        }
        return this;
    }

    public DataportenBodyBuilder withKurl(String kurl) {
        bodyMap.put("KURL", kurl);
        return this;
    }

    public String build() {
        return Try.attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(bodyMap)).orElseThrow();
    }
}
