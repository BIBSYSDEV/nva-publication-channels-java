package no.sikt.nva.pubchannels.dataporten.create;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenCreateJournalRequest {
    private static final String PISSN_FIELD = "pissn";
    private static final String NAME_FIELD = "name";
    private static final String EISSN_FIELD = "eissn";
    private static final String URL_FIELD = "url";
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PISSN_FIELD)
    private final String printIssn;
    @JsonProperty(EISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(URL_FIELD)
    private final String url;

    @JsonCreator
    public DataportenCreateJournalRequest(
            @JsonProperty String name,
            @JsonProperty(PISSN_FIELD) String printIssn,
            @JsonProperty(EISSN_FIELD) String onlineIssn,
            @JsonProperty(URL_FIELD) String url) {
        this.name = name;
        this.printIssn = printIssn;
        this.onlineIssn = onlineIssn;
        this.url = url;
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

    public String getUrl() {
        return url;
    }
}
