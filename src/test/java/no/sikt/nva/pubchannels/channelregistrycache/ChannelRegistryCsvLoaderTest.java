package no.sikt.nva.pubchannels.channelregistrycache;

import static nva.commons.core.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChannelRegistryCsvLoaderTest {

  private static final String EMPTY_STRING = "";
  private ChannelRegistryCsvLoader csvLoader;
  private S3Driver s3Driver;

  @BeforeEach
  void setUp() {
    var s3Client = new FakeS3Client();
    this.s3Driver = new S3Driver(s3Client, ChannelRegistryCacheConfig.CACHE_BUCKET);
    csvLoader = new ChannelRegistryCsvLoader(s3Client);
  }

  @Test
  void shouldThrowExceptionWhenCannotFindPublicationChannel() {
    loadCsv("cache.csv");
    var result = csvLoader.getEntries();
    var cacheEntries = result.entries().toList();

    assertFalse(cacheEntries.isEmpty());
    // Report is available after stream consumption
    var report = result.report().get();
    assertFalse(report.isEmpty());
  }

  @Test
  void ignoreAndReportBadCsv() {
    loadCsv("bad_cache.csv");
    var result = csvLoader.getEntries();
    var cacheEntries = result.entries().toList();

    assertThat(cacheEntries.size(), is(equalTo(1)));
    assertThat(result.report().get(), containsString("Failed to parse 4 out of 5 CSV lines"));
  }

  @Test
  void emptyCsv() {
    attempt(
            () ->
                s3Driver.insertFile(
                    UnixPath.of(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT),
                    EMPTY_STRING))
        .orElseThrow();
    var result = csvLoader.getEntries();
    var cacheEntries = result.entries().toList();

    assertThat(cacheEntries.size(), is(equalTo(0)));
    assertThat(result.report().get(), containsString("No data"));
  }

  private void loadCsv(String csvFile) {
    var csv = IoUtils.stringFromResources(Path.of(csvFile));
    attempt(
            () ->
                s3Driver.insertFile(
                    UnixPath.of(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT), csv))
        .orElseThrow();
  }
}
