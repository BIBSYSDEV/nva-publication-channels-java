package no.sikt.nva.pubchannels.dataporten.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class DataportenEntityResult {

    private static final String IDENTIFIER_FIELD = "pid";
    private static final String NAME_FIELD = "name";
    private static final String PRINT_ISSN_FIELD = "pissn";
    private static final String ONLINE_ISSN_FIELD = "eissn";
    private static final String CURRENT_LEVEL_FIELD = "currentLevel";
    private static final String HOMEPAGE_FIELD = "kURL";

    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PRINT_ISSN_FIELD)
    private final String printIssn;
    @JsonProperty(ONLINE_ISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(CURRENT_LEVEL_FIELD)
    private final DataPortenLevel currentLevel;
    @JsonProperty(HOMEPAGE_FIELD)
    private final URI homepage;

    @JsonCreator
    public DataportenEntityResult(@JsonProperty(IDENTIFIER_FIELD) String identifier,
                                  @JsonProperty(NAME_FIELD) String name,
                                  @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                                  @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                                  @JsonProperty(CURRENT_LEVEL_FIELD) DataPortenLevel currentLevel,
                                  @JsonProperty(HOMEPAGE_FIELD) URI homepage) {
        this.currentLevel = currentLevel;
        this.onlineIssn = onlineIssn;
        this.identifier = identifier;
        this.printIssn = printIssn;
        this.name = name;
        this.homepage = homepage;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public URI getHomepage() {
        return homepage;
    }

    public String getOnlineIssn() {
        return onlineIssn;
    }

    public String getPrintIssn() {
        return printIssn;
    }

    public DataPortenLevel getCurrentLevel() {
        return currentLevel;
    }
}
