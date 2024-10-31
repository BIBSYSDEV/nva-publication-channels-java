package no.sikt.nva.pubchannels.channelregistry.model;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.channelregistry.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.unit.nva.commons.json.JsonSerializable;

@JsonSerialize
public record ChannelRegistryJournal(@JsonProperty(IDENTIFIER_FIELD) String identifier,
                                     @JsonProperty(NAME_FIELD) String name,
                                     @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                                     @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                                     @JsonProperty(LEVEL_FIELD) ChannelRegistryLevel channelRegistryLevel,
                                     @JsonProperty(HOMEPAGE_FIELD) URI homepage,
                                     @JsonProperty(DISCONTINUED) String discontinued)
    implements Immutable, ThirdPartyJournal, JsonSerializable {

    private static final String IDENTIFIER_FIELD = "pid";
    private static final String NAME_FIELD = "originalTitle";
    private static final String ONLINE_ISSN_FIELD = "eissn";
    private static final String PRINT_ISSN_FIELD = "pissn";
    private static final String LEVEL_FIELD = "levelElementDto";
    private static final String HOMEPAGE_FIELD = "kurl";
    private static final String DISCONTINUED = "ceased";
    private static final String CHANNEL_REGISTRY_REVIEW_MARK = "X";
    private static final String NORWEGIAN = "no";
    private static final String ENGLISH = "en";

    @Override
    public String getYear() {
        return Optional.ofNullable(channelRegistryLevel)
                   .map(ChannelRegistryLevel::year)
                   .map(String::valueOf)
                   .orElse(null);
    }

    @Override
    public ScientificValue getScientificValue() {
        return levelToScientificValue(new ScientificValueMapper());
    }

    @Override
    public ScientificValueReviewNotice reviewNotice() {
        return Optional.ofNullable(channelRegistryLevel)
                   .filter(level -> CHANNEL_REGISTRY_REVIEW_MARK.equals(level.levelDisplay()))
                   .map(level -> new ScientificValueReviewNotice(mapLanguages(level)))
                   .orElse(null);
    }

    private static Map<String, String> mapLanguages(ChannelRegistryLevel level) {
        var decisionMap = new HashMap<String, String>();
        if (nonNull(level.decision())) {
            decisionMap.put(ENGLISH, level.decision());
        }
        if (nonNull(level.decisionNO())) {
            decisionMap.put(NORWEGIAN, level.decisionNO());
        }
        return decisionMap;
    }

    private ScientificValue levelToScientificValue(ScientificValueMapper mapper) {
        return Optional.ofNullable(channelRegistryLevel)
                   .map(level -> mapper.map(level.level()))
                   .orElse(ScientificValue.UNASSIGNED);
    }
}
