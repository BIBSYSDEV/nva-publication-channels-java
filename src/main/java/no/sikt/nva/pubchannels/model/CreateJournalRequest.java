package no.sikt.nva.pubchannels.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateJournalRequest {
    private static final String NAME_FIELD = "name";
    private static final String PRINT_ISSN_FIELD = "printIssn";
    private static final String ONLINE_ISSN_FIELD = "onlineIssn";
    private static final String URL_FIELD = "url";

    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PRINT_ISSN_FIELD)
    private final String printIssn;
    @JsonProperty(ONLINE_ISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(URL_FIELD)
    private final String url;

    @JsonCreator
    public CreateJournalRequest(
            @JsonProperty(NAME_FIELD) String name,
            @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
            @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
            @JsonProperty(URL_FIELD)String url
    ) {
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
