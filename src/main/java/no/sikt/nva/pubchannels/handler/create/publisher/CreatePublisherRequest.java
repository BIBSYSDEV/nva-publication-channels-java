package no.sikt.nva.pubchannels.handler.create.publisher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatePublisherRequest(@JsonProperty(NAME_FIELD) String name,
                                     @JsonProperty(ISBN_PREFIX_FIELD) String isbnPrefix,
                                     @JsonProperty(HOMEPAGE_FIELD) String homepage) {

    private static final String NAME_FIELD = "name";
    private static final String ISBN_PREFIX_FIELD = "isbnPrefix";
    private static final String HOMEPAGE_FIELD = "homepage";

    @JsonCreator
    public CreatePublisherRequest {
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String isbnPrefix() {
        return isbnPrefix;
    }

    @Override
    public String homepage() {
        return homepage;
    }
}
