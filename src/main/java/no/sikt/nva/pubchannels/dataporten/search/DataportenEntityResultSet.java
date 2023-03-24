package no.sikt.nva.pubchannels.dataporten.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DataportenEntityResultSet {
    public static final String PAGERESULT_FIELD = "pageresult";

    @JsonProperty(PAGERESULT_FIELD)
    private final List<DataportenJournalResult> pageResult;

    @JsonCreator
    public DataportenEntityResultSet(@JsonProperty(PAGERESULT_FIELD) List<DataportenJournalResult> pageResult) {
        this.pageResult = pageResult;
    }

    public List<DataportenJournalResult> getPageResult() {
        return pageResult;
    }
}
