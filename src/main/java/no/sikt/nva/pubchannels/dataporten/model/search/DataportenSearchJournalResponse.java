package no.sikt.nva.pubchannels.dataporten.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.dataporten.model.fetch.DataportenJournal;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;

public class DataportenSearchJournalResponse implements ThirdPartySearchResponse {

    private static final String ENTITY_RESULT_SET = "entityResultSet";
    private static final String ENTITY_PAGE_INFORMATION = "entityPageInformation";
    @JsonProperty(ENTITY_PAGE_INFORMATION)
    private final DataPortenEntityPageInformation pageInformation;

    @JsonProperty(ENTITY_RESULT_SET)
    private final DataportenEntityResultSet<DataportenJournal> resultSet;

    @JsonCreator
    public DataportenSearchJournalResponse(
        @JsonProperty(ENTITY_PAGE_INFORMATION) DataPortenEntityPageInformation pageInformation,
        @JsonProperty(ENTITY_RESULT_SET) DataportenEntityResultSet<DataportenJournal> resultSet
    ) {
        this.pageInformation = pageInformation;
        this.resultSet = resultSet;
    }

    public DataportenEntityResultSet<DataportenJournal> getResultSet() {
        return resultSet;
    }

    public DataPortenEntityPageInformation getPageInformation() {
        return pageInformation;
    }
}
