package no.sikt.nva.pubchannels.dataporten;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JournalRequestBody {
    @JsonProperty
    private final String name;

    @JsonCreator
    public JournalRequestBody(@JsonProperty String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
