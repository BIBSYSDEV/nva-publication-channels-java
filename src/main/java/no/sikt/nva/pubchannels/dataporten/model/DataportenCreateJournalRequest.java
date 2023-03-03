package no.sikt.nva.pubchannels.dataporten.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenCreateJournalRequest {
    private static final String PISSN_FIELD = "pissn";
    private static final String NAME_FIELD = "name";
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PISSN_FIELD)
    private final String printIssn;

    @JsonCreator
    public DataportenCreateJournalRequest(@JsonProperty String name, @JsonProperty(PISSN_FIELD) String printIssn) {
        this.name = name;
        this.printIssn = printIssn;
    }

    public String getName() {
        return name;
    }

    public String getPrintIssn() {
        return printIssn;
    }
}
