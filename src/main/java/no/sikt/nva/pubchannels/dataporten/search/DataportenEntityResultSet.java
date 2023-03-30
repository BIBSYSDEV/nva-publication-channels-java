package no.sikt.nva.pubchannels.dataporten.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DataportenEntityResultSet {
    private static final String PAGERESULT_FIELD = "pageresult";

    @JsonProperty(PAGERESULT_FIELD)
    private final List<DataportenEntityResult> pageResult;

    @JsonCreator
    public DataportenEntityResultSet(@JsonProperty(PAGERESULT_FIELD) List<DataportenEntityResult> pageResult) {
        this.pageResult = pageResult;
    }

    public List<DataportenEntityResult> getPageResult() {
        return pageResult;
    }
}
