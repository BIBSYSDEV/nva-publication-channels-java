package no.sikt.nva.pubchannels.channelRegistry.model.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record ChannelRegistryCreateSeriesRequest(@JsonProperty(NAME_FIELD) String name,
                                                 @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                                                 @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                                                 @JsonProperty(HOMEPAGE_FIELD) String homepage) {

    private static final String NAME_FIELD = "name";
    private static final String PRINT_ISSN_FIELD = "pissn";
    private static final String ONLINE_ISSN_FIELD = "eissn";
    private static final String HOMEPAGE_FIELD = "url";
}
