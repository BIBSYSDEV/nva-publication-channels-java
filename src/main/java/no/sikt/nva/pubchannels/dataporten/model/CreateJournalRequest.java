package no.sikt.nva.pubchannels.dataporten.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateJournalRequest {
    @JsonProperty
    private final String name;

    @JsonCreator
    public CreateJournalRequest(@JsonProperty String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
