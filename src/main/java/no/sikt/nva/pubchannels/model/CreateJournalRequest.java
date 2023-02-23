package no.sikt.nva.pubchannels.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateJournalRequest {
    private static final String NAME_FIELD = "name";
    private static final String PISSN_FIELD = "pissn";
    private static final String EISSN_FIELD = "eissn";

    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PISSN_FIELD)
    private final String pissn;
    @JsonProperty(EISSN_FIELD)
    private final String eissn;

    @JsonCreator
    public CreateJournalRequest(
            @JsonProperty(NAME_FIELD) String name,
            @JsonProperty(PISSN_FIELD) String pissn,
            @JsonProperty(EISSN_FIELD) String eissn
    ) {
        this.name = name;
        this.pissn = pissn;
        this.eissn = eissn;
    }

    public String getName() {
        return name;
    }

    public String getPissn() {
        return pissn;
    }

    public String getEissn() {
        return eissn;
    }
}
