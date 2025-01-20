package no.sikt.nva.pubchannels.channelregistry.model;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.channelregistry.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.unit.nva.commons.json.JsonSerializable;

@JsonSerialize
public record ChannelRegistryPublisher(
    @JsonProperty(IDENTIFIER_FIELD) String identifier,
    @JsonProperty(LEVEL_FIELD) ChannelRegistryLevel channelRegistryLevel,
    @JsonProperty(ISBN_PREFIX_FIELD) String isbnPrefix,
    @JsonProperty(NAME_FIELD) String name,
    @JsonProperty(HOMEPAGE_FIELD) URI homepage,
    @JsonProperty(DISCONTINUED_FIELD) String discontinued,
    @JsonProperty(TYPE_FIELD) String type)
    implements Immutable, ThirdPartyPublisher, JsonSerializable {

    private static final String IDENTIFIER_FIELD = "pid";
    private static final String NAME_FIELD = "name";
    private static final String ISBN_PREFIX_FIELD = "isbnprefix";
    private static final String LEVEL_FIELD = "levelElementDto";
    private static final String HOMEPAGE_FIELD = "kurl";
    private static final String DISCONTINUED_FIELD = "ceased";
    private static final String TYPE_FIELD = "type";
    private static final String PUBLISHER = "Publisher";

    @Override
    public String getYear() {
        return Optional
                   .ofNullable(channelRegistryLevel)
                   .map(ChannelRegistryLevel::year)
                   .map(String::valueOf)
                   .orElse(null);
    }

    @Override
    public String type() {
        if ("publisher".equalsIgnoreCase(type)) {
            return PUBLISHER;
        } else {
            throw new IllegalArgumentException("Unknown type found. Expected 'publisher'.");
        }
    }

    @Override
    public ScientificValue getScientificValue() {
        return levelToScientificValue(new ScientificValueMapper());
    }

    @Override
    public ScientificValueReviewNotice reviewNotice() {
        return nonNull(channelRegistryLevel) ? channelRegistryLevel.reviewNotice() : null;
    }

    private ScientificValue levelToScientificValue(ScientificValueMapper mapper) {
        return Optional
                   .ofNullable(channelRegistryLevel())
                   .map(level -> mapper.map(level.level()))
                   .orElse(ScientificValue.UNASSIGNED);
    }
}
