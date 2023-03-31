package no.sikt.nva.pubchannels.dataporten.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenSearchJournalResponse {
    @JsonProperty("entityPageInformation")
    private final DataPortenEntityPageInformation pageInformation;

    @JsonProperty("entityResultSet")
    private final DataportenEntityResultSet resultSet;

    @JsonCreator
    public DataportenSearchJournalResponse(
            @JsonProperty("entityPageInformation") DataPortenEntityPageInformation pageInformation,
            @JsonProperty("entityResultSet") DataportenEntityResultSet resultSet
    ) {
        this.pageInformation = pageInformation;
        this.resultSet = resultSet;
    }

    public DataPortenEntityPageInformation getPageInformation() {
        return pageInformation;
    }

    public DataportenEntityResultSet getResultSet() {
        return resultSet;
    }
}
