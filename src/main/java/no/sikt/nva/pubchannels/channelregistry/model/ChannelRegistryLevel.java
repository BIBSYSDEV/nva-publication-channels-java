package no.sikt.nva.pubchannels.channelregistry.model;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;

@JsonSerialize
public record ChannelRegistryLevel(
    Integer year,
    String level,
    String levelDisplay,
    @JsonProperty("vedtak") String decisionNO,
    String decision) {

  private static final String CHANNEL_REGISTRY_REVIEW_MARK = "X";
  private static final String NORWEGIAN = "no";
  private static final String ENGLISH = "en";

  public ScientificValueReviewNotice reviewNotice() {
    return Optional.ofNullable(levelDisplay)
        .filter(levelDisplay -> CHANNEL_REGISTRY_REVIEW_MARK.equals(levelDisplay()))
        .map(level -> new ScientificValueReviewNotice(mapLanguages()))
        .orElse(null);
  }

  private Map<String, String> mapLanguages() {
    var decisionMap = new HashMap<String, String>();
    if (nonNull(decision)) {
      decisionMap.put(ENGLISH, decision);
    }
    if (nonNull(decisionNO)) {
      decisionMap.put(NORWEGIAN, decisionNO);
    }
    return decisionMap;
  }
}
