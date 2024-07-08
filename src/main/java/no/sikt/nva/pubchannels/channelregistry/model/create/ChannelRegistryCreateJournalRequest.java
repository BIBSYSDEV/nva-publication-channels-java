package no.sikt.nva.pubchannels.channelregistry.model.create;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChannelRegistryCreateJournalRequest(@JsonProperty(NAME_FIELD) String name,
                                                  @JsonProperty(PISSN_FIELD) String printIssn,
                                                  @JsonProperty(EISSN_FIELD) String onlineIssn,
                                                  @JsonProperty(URL_FIELD) String url) {

    private static final String PISSN_FIELD = "pissn";
    private static final String NAME_FIELD = "name";
    private static final String EISSN_FIELD = "eissn";
    private static final String URL_FIELD = "url";
}
