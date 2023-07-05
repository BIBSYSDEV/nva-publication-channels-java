package no.sikt.nva.pubchannels.dataporten.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.handler.search.ThirdPartyPageInformation;

public class DataPortenEntityPageInformation implements ThirdPartyPageInformation {

    public static final String TOTAL_RESULTS_FIELD = "totalResults";

    @JsonProperty(TOTAL_RESULTS_FIELD)
    private final Integer totalResults;

    @JsonCreator
    public DataPortenEntityPageInformation(@JsonProperty(TOTAL_RESULTS_FIELD) Integer totalResults) {
        this.totalResults = totalResults;
    }

    @Override
    public Integer getTotalResults() {
        return totalResults;
    }
}
