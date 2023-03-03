package no.sikt.nva.pubchannels.handler.create.publisher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePublisherRequest {
    private static final String NAME_FIELD = "name";
    private static final String PRINT_ISSN_FIELD = "printIssn";
    private static final String ONLINE_ISSN_FIELD = "onlineIssn";
    private static final String HOMEPAGE_FIELD = "homepage";

    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PRINT_ISSN_FIELD)
    private final String printIssn;
    @JsonProperty(ONLINE_ISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(HOMEPAGE_FIELD)
    private final String homepage;

    @JsonCreator
    public CreatePublisherRequest(
            @JsonProperty(NAME_FIELD) String name,
            @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
            @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
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
