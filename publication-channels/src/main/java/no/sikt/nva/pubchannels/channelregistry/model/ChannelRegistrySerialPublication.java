package no.sikt.nva.pubchannels.channelregistry.model;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.channelregistry.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.unit.nva.commons.json.JsonSerializable;

@JsonSerialize
public record ChannelRegistrySerialPublication(
    @JsonProperty(IDENTIFIER_FIELD) String identifier,
    @JsonProperty(NAME_FIELD) String name,
    @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
    @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
    @JsonProperty(LEVEL_FIELD) ChannelRegistryLevel channelRegistryLevel,
    @JsonProperty(HOMEPAGE_FIELD) URI homepage,
    @JsonProperty(DISCONTINUED_FIELD) String discontinued,
    @JsonProperty(TYPE_FIELD) String type)
    implements Immutable, ThirdPartySerialPublication, JsonSerializable {

  private static final String IDENTIFIER_FIELD = "pid";
  private static final String NAME_FIELD = "originalTitle";
  private static final String ONLINE_ISSN_FIELD = "eissn";
  private static final String PRINT_ISSN_FIELD = "pissn";
  private static final String LEVEL_FIELD = "levelElementDto";
  private static final String HOMEPAGE_FIELD = "kurl";
  private static final String DISCONTINUED_FIELD = "ceased";
  private static final String TYPE_FIELD = "type";

  @Override
  public Optional<String> getYear() {
    return Optional.ofNullable(channelRegistryLevel())
        .map(ChannelRegistryLevel::year)
        .map(String::valueOf);
  }

  @Override
  public ScientificValue getScientificValue() {
    return levelToScientificValue(new ScientificValueMapper());
  }

  @Override
  public ScientificValueReviewNotice reviewNotice() {
    return nonNull(channelRegistryLevel) ? channelRegistryLevel.reviewNotice() : null;
  }

  @Override
  public String type() {
    return switch (type.toLowerCase(Locale.ROOT)) {
      case "journal", "tidsskrift" -> "Journal";
      case "series", "serie" -> "Series";
      case null, default ->
          throw new IllegalArgumentException(
              "Unknown type found. Expected one of ['journal', " + "'series'].");
    };
  }

  private ScientificValue levelToScientificValue(ScientificValueMapper mapper) {
    return Optional.ofNullable(channelRegistryLevel())
        .map(level -> mapper.map(level.level()))
        .orElse(ScientificValue.UNASSIGNED);
  }
}
