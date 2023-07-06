package no.sikt.nva.pubchannels.dataporten.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.dataporten.model.DataportenSeries;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public class DataportenSearchSeriesResponse implements ThirdPartySearchResponse {

    private static final String ENTITY_RESULT_SET = "entityResultSetDto";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformationDto";
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

    @Override
    public DataportenEntityResultSet<DataportenSeries> getResultSet() {
        return resultSet;
    }

    @Override
    public DataPortenEntityPageInformation getPageInformation() {
        return pageInformation;
    }
}
