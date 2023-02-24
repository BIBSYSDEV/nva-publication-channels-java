package no.sikt.nva.pubchannels.dataporten.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.Immutable;

import java.net.URI;

public class FetchJournalByIdentifierDto implements Immutable {
    private static final String IDENTIFIER_FIELD = "pid";
    private static final String NAME_FIELD = "name";
    private static final String ONLINE_ISSN_FIELD = "eissn";
    private static final String PRINT_ISSN_FIELD = "pissn";
    private static final String CURRENT_LEVEL_FIELD = "current";
    private static final String HOMEPAGE_FIELD = "kURL";

    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(ONLINE_ISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(PRINT_ISSN_FIELD)
    private final String printIssn;
    @JsonProperty(CURRENT_LEVEL_FIELD)
    private final DataportenLevel current;
    @JsonProperty(HOMEPAGE_FIELD)
    private final URI homepage;

    @JsonCreator
    public FetchJournalByIdentifierDto(@JsonProperty(IDENTIFIER_FIELD) String identifier,
                                       @JsonProperty(NAME_FIELD) String name,
                                       @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                                       @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                                       @JsonProperty(CURRENT_LEVEL_FIELD) DataportenLevel current,
                                       @JsonProperty(HOMEPAGE_FIELD) URI homepage) {
        this.identifier = identifier;
        this.name = name;
        this.onlineIssn = onlineIssn;
        this.printIssn = printIssn;
        this.current = current;
        this.homepage = homepage;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getOnlineIssn() {
        return onlineIssn;
    }

    public String getPrintIssn() {
        return printIssn;
    }

    public DataportenLevel getCurrent() {
        return current;
    }

    public URI getHomepage() {
        return homepage;
    }

}
