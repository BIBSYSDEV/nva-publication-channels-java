package no.sikt.nva.pubchannels.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateJournalRequest {
    private static final String NAME_FIELD = "name";

    @JsonProperty(NAME_FIELD)
    private final String name;

    @JsonCreator
    public CreateJournalRequest(
            @JsonProperty(NAME_FIELD) String name
    ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
