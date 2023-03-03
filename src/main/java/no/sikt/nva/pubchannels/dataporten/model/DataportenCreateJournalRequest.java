package no.sikt.nva.pubchannels.dataporten.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenCreateJournalRequest {
    private static final String PISSN_FIELD = "pissn";
    private static final String NAME_FIELD = "name";
    private static final String EISSN_FIELD = "eissn";
    private static final String HOMEPAGE_FIELD = "homepage";
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PISSN_FIELD)
    private final String printIssn;
    @JsonProperty(EISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(HOMEPAGE_FIELD)
    private final String homepage;

    @JsonCreator
    public DataportenCreateJournalRequest(
            @JsonProperty String name,
            @JsonProperty(PISSN_FIELD) String printIssn,
            @JsonProperty(EISSN_FIELD) String onlineIssn,
            @JsonProperty(HOMEPAGE_FIELD) String homepage) {
        this.name = name;
        this.printIssn = printIssn;
        this.onlineIssn = onlineIssn;
        this.homepage = homepage;
    }

    public String getName() {
        return name;
    }

    public String getPrintIssn() {
        return printIssn;
    }

    public String getOnlineIssn() {
        return onlineIssn;
    }

    public String getHomepage() {
        return homepage;
    }
}
