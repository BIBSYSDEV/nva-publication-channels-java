package no.sikt.nva.pubchannels.dataporten.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataPortenLevel {

    public static final String YEAR_FIELD = "year";
    public static final String LEVEL_FIELD = "level";

    @JsonProperty(YEAR_FIELD)
    private final int year;
    @JsonProperty(LEVEL_FIELD)
    private final String level;

    @JsonCreator
    public DataPortenLevel(@JsonProperty(YEAR_FIELD) int year, @JsonProperty(LEVEL_FIELD) String level) {
        this.year = year;
        this.level = level;
    }

    public int getYear() {
        return year;
    }

    public String getLevel() {
        return level;
    }
}
