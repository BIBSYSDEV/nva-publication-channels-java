package no.sikt.nva.pubchannels.handler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import nva.commons.core.SingletonCollector;

public enum ScientificValue {
  UNASSIGNED("Unassigned"),
  LEVEL_ZERO("LevelZero"),
  LEVEL_ONE("LevelOne"),
  LEVEL_TWO("LevelTwo");

  private final String serializedValue;

  ScientificValue(String serializedValue) {
    this.serializedValue = serializedValue;
  }

  @JsonValue
  public String getSerializedValue() {
    return serializedValue;
  }

  @JsonCreator
  public static ScientificValue lookup(String serializedValue) {
    return Arrays.stream(values())
        .filter(item -> item.serializedValue.equalsIgnoreCase(serializedValue))
        .collect(SingletonCollector.collectOrElse(UNASSIGNED));
  }
}
