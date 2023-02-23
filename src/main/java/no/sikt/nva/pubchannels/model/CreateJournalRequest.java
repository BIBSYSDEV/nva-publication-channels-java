package no.sikt.nva.pubchannels.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateJournalRequest {
    private static final String NAME_FIELD = "name";
    private static final String PISSN_FIELD = "pissn";
    private static final String EISSN_FIELD = "eissn";
    private static final String URL_FIELD = "url";

    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(PISSN_FIELD)
    private final String pissn;
    @JsonProperty(EISSN_FIELD)
    private final String eissn;
    @JsonProperty(URL_FIELD)
    private final String url;

    @JsonCreator
    public CreateJournalRequest(
            @JsonProperty(NAME_FIELD) String name,
            @JsonProperty(PISSN_FIELD) String pissn,
            @JsonProperty(EISSN_FIELD) String eissn,
            @JsonProperty(URL_FIELD)String url
    ) {
        this.name = name;
        this.pissn = pissn;
        this.eissn = eissn;

        this.url = url;
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

    public String getUrl() {
        return url;
    }
}
