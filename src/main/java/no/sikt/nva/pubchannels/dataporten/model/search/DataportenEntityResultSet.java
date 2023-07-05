package no.sikt.nva.pubchannels.dataporten.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import no.sikt.nva.pubchannels.handler.search.ThirdPartyResultSet;

public class DataportenEntityResultSet<T> implements ThirdPartyResultSet<T> {

    private static final String PAGERESULT_FIELD = "pageresult";

    @JsonProperty(PAGERESULT_FIELD)
    private final List<T> pageResult;

    @JsonCreator
    public DataportenEntityResultSet(@JsonProperty(PAGERESULT_FIELD) List<T> pageResult) {
        this.pageResult = pageResult;
    }

    public List<T> getPageResult() {
        return pageResult;
    }
}
