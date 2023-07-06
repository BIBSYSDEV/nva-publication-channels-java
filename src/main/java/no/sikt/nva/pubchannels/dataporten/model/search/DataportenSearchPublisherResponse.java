package no.sikt.nva.pubchannels.dataporten.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.dataporten.model.DataportenPublisher;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public class DataportenSearchPublisherResponse implements ThirdPartySearchResponse {

    private static final String ENTITY_RESULT_SET = "entityResultSetDto";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformationDto";
    @JsonProperty(ENTITY_PAGE_INFORMATION)
    private final DataPortenEntityPageInformation pageInformation;

    @JsonProperty(ENTITY_RESULT_SET)
    private final DataportenEntityResultSet<DataportenPublisher> resultSet;

    @JsonCreator
    public DataportenSearchPublisherResponse(
        @JsonProperty(ENTITY_PAGE_INFORMATION) DataPortenEntityPageInformation pageInformation,
        @JsonProperty(ENTITY_RESULT_SET) DataportenEntityResultSet<DataportenPublisher> resultSet
    ) {
        this.pageInformation = pageInformation;
        this.resultSet = resultSet;
    }

    @Override
    public DataportenEntityResultSet<DataportenPublisher> getResultSet() {
        return resultSet;
    }

    @Override
    public DataPortenEntityPageInformation getPageInformation() {
        return pageInformation;
    }
}
