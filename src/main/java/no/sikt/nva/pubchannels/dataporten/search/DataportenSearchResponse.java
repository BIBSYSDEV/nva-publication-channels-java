package no.sikt.nva.pubchannels.dataporten.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenSearchResponse {
    private static final String ENTITY_RESULT_SET = "entityResultSet";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformation";
    @JsonProperty(ENTITY_PAGE_INFORMATION)
    private final DataPortenEntityPageInformation pageInformation;

    @JsonProperty(ENTITY_RESULT_SET)
    private final DataportenEntityResultSet resultSet;

    @JsonCreator
    public DataportenSearchResponse(
            @JsonProperty(ENTITY_PAGE_INFORMATION) DataPortenEntityPageInformation pageInformation,
            @JsonProperty(ENTITY_RESULT_SET) DataportenEntityResultSet resultSet
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
