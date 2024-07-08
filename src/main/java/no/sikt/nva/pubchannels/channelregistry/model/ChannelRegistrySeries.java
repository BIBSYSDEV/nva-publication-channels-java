package no.sikt.nva.pubchannels.channelregistry.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.channelregistry.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;

@JsonSerialize
public record ChannelRegistrySeries(@JsonProperty(IDENTIFIER_FIELD) String identifier,
                                    @JsonProperty(NAME_FIELD) String name,
                                    @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                                    @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                                    @JsonProperty(LEVEL_FIELD) ChannelRegistryLevel channelRegistryLevel,
                                    @JsonProperty(HOMEPAGE_FIELD) URI homepage,
                                    @JsonProperty(DISCONTINUED) String discontinued) implements Immutable, ThirdPartySeries {

    private static final String IDENTIFIER_FIELD = "pid";
    private static final String NAME_FIELD = "originalTitle";
    private static final String ONLINE_ISSN_FIELD = "eissn";
    private static final String PRINT_ISSN_FIELD = "pissn";
    private static final String LEVEL_FIELD = "levelElementDto";
    private static final String HOMEPAGE_FIELD = "kurl";
    public static final String DISCONTINUED = "ceased";

    @Override
    public String getYear() {
        return Optional.ofNullable(channelRegistryLevel())
                   .map(ChannelRegistryLevel::year)
                   .map(String::valueOf)
                   .orElse(null);
    }

    @Override
    public ScientificValue getScientificValue() {
        return levelToScientificValue(new ScientificValueMapper());
    }

    private ScientificValue levelToScientificValue(ScientificValueMapper mapper) {
        return Optional.ofNullable(channelRegistryLevel())
                   .map(level -> mapper.map(level.level()))
                   .orElse(ScientificValue.UNASSIGNED);
    }
}
