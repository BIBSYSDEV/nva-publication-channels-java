package no.sikt.nva.pubchannels.dataporten.create;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenCreatePublisherRequest {
    private static final String NAME_FIELD = "name";
    private static final String PRINTISSN_FIELD = "pissn";
    private static final String ONLINE_ISSN_FIELD = "eissn";
    private static final String URL_FIELD = "url";
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PRINTISSN_FIELD)
    private final String printIssn;
    @JsonProperty(ONLINE_ISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(URL_FIELD)
    private final String homepage;

    @JsonCreator
    public DataportenCreatePublisherRequest(
            @JsonProperty(NAME_FIELD) String name,
            @JsonProperty(PRINTISSN_FIELD) String printIssn,
            @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
            @JsonProperty(URL_FIELD) String homepage) {
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
