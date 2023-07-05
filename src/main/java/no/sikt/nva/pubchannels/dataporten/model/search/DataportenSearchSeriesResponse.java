package no.sikt.nva.pubchannels.dataporten.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.dataporten.model.fetch.DataportenSeries;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public class DataportenSearchSeriesResponse implements ThirdPartySearchResponse {

    private static final String ENTITY_RESULT_SET = "entityResultSet";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformation";
    @JsonProperty(ENTITY_PAGE_INFORMATION)
    private final DataPortenEntityPageInformation pageInformation;

    @JsonProperty(ENTITY_RESULT_SET)
    private final DataportenEntityResultSet<DataportenSeries> resultSet;

    @JsonCreator
    public DataportenSearchSeriesResponse(
        @JsonProperty(ENTITY_PAGE_INFORMATION) DataPortenEntityPageInformation pageInformation,
        @JsonProperty(ENTITY_RESULT_SET) DataportenEntityResultSet<DataportenSeries> resultSet
    ) {
        this.pageInformation = pageInformation;
        this.resultSet = resultSet;
    }

    public DataportenEntityResultSet<DataportenSeries> getResultSet() {
        return resultSet;
    }

    public DataPortenEntityPageInformation getPageInformation() {
        return pageInformation;
    }
}
