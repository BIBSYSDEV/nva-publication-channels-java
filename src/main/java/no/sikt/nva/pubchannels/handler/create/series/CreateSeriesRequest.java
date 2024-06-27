package no.sikt.nva.pubchannels.handler.create.series;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateSeriesRequest(@JsonProperty(NAME_FIELD) String name,
                                  @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                                  @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                                  @JsonProperty(HOMEPAGE_FIELD) String homepage) {

    private static final String NAME_FIELD = "name";
    private static final String PRINT_ISSN_FIELD = "printIssn";
    private static final String ONLINE_ISSN_FIELD = "onlineIssn";
    private static final String HOMEPAGE_FIELD = "homepage";

    @JsonCreator
    public CreateSeriesRequest {
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String printIssn() {
        return printIssn;
    }

    @Override
    public String onlineIssn() {
        return onlineIssn;
    }

    @Override
    public String homepage() {
        return homepage;
    }
}
