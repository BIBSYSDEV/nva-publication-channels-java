package no.sikt.nva.pubchannels.dataporten.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataPortenEntityPageInformation {
    public static final String TOTAL_RESULTS_FIELD = "totalResults";

    @JsonProperty(TOTAL_RESULTS_FIELD)
    private final Integer totalResults;

    @JsonCreator
    public DataPortenEntityPageInformation(@JsonProperty(TOTAL_RESULTS_FIELD) Integer totalResults) {
        this.totalResults = totalResults;
    }

    public Integer getTotalResults() {
        return totalResults;
    }
}
