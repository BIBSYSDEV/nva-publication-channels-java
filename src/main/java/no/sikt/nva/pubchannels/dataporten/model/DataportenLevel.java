package no.sikt.nva.pubchannels.dataporten.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenLevel {

    public static final String YEAR_FIELD = "year";
    public static final String LEVEL_FIELD = "level";
    @JsonProperty(YEAR_FIELD)
    private final Integer year;
    @JsonProperty(LEVEL_FIELD)
    private final String level;

    @JsonCreator
    public DataportenLevel(@JsonProperty(YEAR_FIELD) Integer year, @JsonProperty(LEVEL_FIELD) String level) {
        this.year = year;
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public Integer getYear() {
        return year;
    }
}
