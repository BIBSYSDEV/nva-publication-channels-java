package no.sikt.nva.pubchannels.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import no.sikt.nva.pubchannels.dataporten.model.DataportenLevel;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.attempt.Try;

public class DataportenBodyBuilder {
    private final Map<String, Object> bodyMap = new ConcurrentHashMap<>();

    public DataportenBodyBuilder() {
        bodyMap.put("type", "Journal");
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

    public DataportenBodyBuilder withLevels(List<DataportenLevel> levels) {
        bodyMap.put("levels", levels);
        return this;
    }

    public DataportenBodyBuilder withCurrent(DataportenLevel currentLevel) {
        bodyMap.put("current", currentLevel);
        return this;
    }

    public String build() {
        return Try.attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(bodyMap)).orElseThrow();
    }
}
