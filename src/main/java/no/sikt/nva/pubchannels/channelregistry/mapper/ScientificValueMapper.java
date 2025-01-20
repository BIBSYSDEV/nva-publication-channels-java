package no.sikt.nva.pubchannels.channelregistry.mapper;

import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.handler.ScientificValue.LEVEL_ONE;
import static no.sikt.nva.pubchannels.handler.ScientificValue.LEVEL_TWO;
import static no.sikt.nva.pubchannels.handler.ScientificValue.LEVEL_ZERO;
import static no.sikt.nva.pubchannels.handler.ScientificValue.UNASSIGNED;
import java.util.Map;
import no.sikt.nva.pubchannels.handler.ScientificValue;

public class ScientificValueMapper {

  public static final Map<String, ScientificValue> VALUES =
      Map.of("0", LEVEL_ZERO, "1", LEVEL_ONE, "2", LEVEL_TWO);

  public ScientificValue map(String value) {
    return nonNull(value) ? VALUES.get(value) : UNASSIGNED;
  }
}
