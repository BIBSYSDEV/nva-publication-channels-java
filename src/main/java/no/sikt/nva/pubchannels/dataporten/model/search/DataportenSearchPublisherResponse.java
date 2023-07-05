package no.sikt.nva.pubchannels.dataporten.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.dataporten.model.fetch.DataportenPublisher;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public class DataportenSearchPublisherResponse implements ThirdPartySearchResponse {

    private static final String ENTITY_RESULT_SET = "entityResultSet";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformation";
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

    public DataportenEntityResultSet<DataportenPublisher> getResultSet() {
        return resultSet;
    }

    public DataPortenEntityPageInformation getPageInformation() {
        return pageInformation;
    }
}
