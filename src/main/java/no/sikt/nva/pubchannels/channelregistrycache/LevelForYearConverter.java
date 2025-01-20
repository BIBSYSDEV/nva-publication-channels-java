package no.sikt.nva.pubchannels.channelregistrycache;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.util.Arrays;
import java.util.List;
import no.unit.nva.commons.json.JsonUtils;

public class LevelForYearConverter extends AbstractBeanField<List<LevelForYear>, String> {

  @Override
  public List<LevelForYear> convert(String value)
      throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    try {
      return nonNull(value) ? convertStringToList(value) : List.of();
    } catch (Exception e) {
      throw new CsvDataTypeMismatchException(e.getMessage());
    }
  }

  private List<LevelForYear> convertStringToList(String value) throws JsonProcessingException {
    var stringAsList = String.format("[%s]", value);
    return Arrays.asList(JsonUtils.dtoObjectMapper.readValue(stringAsList, LevelForYear[].class));
  }
}
