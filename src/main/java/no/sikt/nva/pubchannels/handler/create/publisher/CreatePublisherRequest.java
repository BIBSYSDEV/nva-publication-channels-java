package no.sikt.nva.pubchannels.handler.create.publisher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePublisherRequest {

    private static final String NAME_FIELD = "name";
    private static final String ISBN_PREFIX_FIELD = "isbnprefix";
    private static final String HOMEPAGE_FIELD = "homepage";

    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(ISBN_PREFIX_FIELD)
    private final String isbnPrefix;
    @JsonProperty(HOMEPAGE_FIELD)
    private final String homepage;

    @JsonCreator
    public CreatePublisherRequest(
        @JsonProperty(NAME_FIELD) String name,
        @JsonProperty(ISBN_PREFIX_FIELD) String isbnPrefix,
        @JsonProperty(HOMEPAGE_FIELD) String homepage) {
        this.name = name;
        this.isbnPrefix = isbnPrefix;
        this.homepage = homepage;
    }

    public String getName() {
        return name;
    }

    public String getIsbnPrefix() {
        return isbnPrefix;
    }

    public String getHomepage() {
        return homepage;
    }
}
