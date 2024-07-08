package no.sikt.nva.pubchannels.channelregistry.model.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record ChannelRegistryCreatePublisherRequest(@JsonProperty(NAME_FIELD) String name,
                                                    @JsonProperty(ISBN_PREFIX_FIELD) String isbnPrefix,
                                                    @JsonProperty(URL_FIELD) String homepage) {

    private static final String NAME_FIELD = "name";
    private static final String ISBN_PREFIX_FIELD = "isbnprefix";
    private static final String URL_FIELD = "url";
}
