package no.sikt.nva.pubchannels.dataporten.mapper;

import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.handler.ScientificValue.LEVEL_ONE;
import static no.sikt.nva.pubchannels.handler.ScientificValue.LEVEL_TWO;
import static no.sikt.nva.pubchannels.handler.ScientificValue.LEVEL_ZERO;
import static no.sikt.nva.pubchannels.handler.ScientificValue.UNASSIGNED;
import java.util.Map;
import no.sikt.nva.pubchannels.handler.ScientificValue;

public class ScientificValueMapper implements Mapper {

    @Override
    public ScientificValue map(String value) {
        var values = Map.of("0", LEVEL_ZERO, "1", LEVEL_ONE, "2", LEVEL_TWO);
        return nonNull(value) ? values.get(value) : UNASSIGNED;
    }
}
