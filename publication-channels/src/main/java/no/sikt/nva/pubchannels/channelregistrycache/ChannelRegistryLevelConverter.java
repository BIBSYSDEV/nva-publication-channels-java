package no.sikt.nva.pubchannels.channelregistrycache;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import no.unit.nva.commons.json.JsonUtils;

public class ChannelRegistryLevelConverter extends AbstractBeanField<LevelForYear, String> {

  @Override
  public LevelForYear convert(String value) throws CsvDataTypeMismatchException {
    try {
      return nonNull(value) ? convertStringToLevel(value) : null;
    } catch (JsonProcessingException e) {
      throw new CsvDataTypeMismatchException(e.getMessage());
    }
  }

  private LevelForYear convertStringToLevel(String value) throws JsonProcessingException {
    return JsonUtils.dtoObjectMapper.readValue(value, LevelForYear.class);
  }
}
