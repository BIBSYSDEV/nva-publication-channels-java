package no.sikt.nva.pubchannels.channelregistrycache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChannelRegistryCacheEntryTest {

  public static final String TEST_CSV = "src/test/resources/cache.csv";

  @Test
  void shouldParseCsvToListOfBeans() throws FileNotFoundException {
    var beans = parseTestCsv();

    assertTrue(beans.stream().allMatch(Objects::nonNull));
  }

  @Test
  void shouldExtractCurrentLevel() throws FileNotFoundException {
    var channelWithCurrentLevel = UUID.fromString("6FB93CFA-DC69-482C-A73A-FC8183CDE8A4");
    var currentLevel =
        parseTestCsv().stream()
            .filter(entry -> channelWithCurrentLevel.equals(entry.getPid()))
            .map(ChannelRegistryCacheEntry::getCurrentLevel)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow();

    assertThat(currentLevel.year(), is("2024"));
    assertThat(currentLevel.level(), is("0"));
  }

  @Test
  void shouldExtractNoCurrentLevelWhenColumnIsEmpty() throws FileNotFoundException {
    var channelWithoutCurrentLevel = UUID.fromString("09D6F92E-B0F6-4B62-90AB-1B9E767E9E11");
    var channel =
        parseTestCsv().stream()
            .filter(entry -> channelWithoutCurrentLevel.equals(entry.getPid()))
            .findFirst()
            .orElseThrow();

    assertThat(channel.getCurrentLevel(), is(nullValue()));
  }

  private static List<ChannelRegistryCacheEntry> parseTestCsv() throws FileNotFoundException {
    return new CsvToBeanBuilder<ChannelRegistryCacheEntry>(new FileReader(TEST_CSV))
        .withType(ChannelRegistryCacheEntry.class)
        .withSeparator(';')
        .withIgnoreEmptyLine(true)
        .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
        .build()
        .parse();
  }
}
