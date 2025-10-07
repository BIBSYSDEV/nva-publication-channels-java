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
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChannelRegistryCsvLoaderTest {

  private ChannelRegistryCsvLoader csvLoader;

  @BeforeEach
  void setUp() {
    var csv = IoUtils.stringFromResources(Path.of("cache.csv"));
    var s3Client = new FakeS3Client();
    var s3Driver = new S3Driver(s3Client, ChannelRegistryCacheConfig.CACHE_BUCKET);
    attempt(
            () ->
                s3Driver.insertFile(
                    UnixPath.of(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT), csv))
        .orElseThrow();
    csvLoader = ChannelRegistryCsvLoader.load(s3Client);
  }

  @Test
  void shouldThrowExceptionWhenCannotFindPublicationChannel() {
    var cacheEntries = csvLoader.getCacheEntries();

    assertFalse(cacheEntries.isEmpty());
  }

  @Test
  void ignoreAndReportBadCsv() {
    var csv = IoUtils.stringFromResources(Path.of("bad_cache.csv"));
    var s3Client = new FakeS3Client();
    var s3Driver = new S3Driver(s3Client, ChannelRegistryCacheConfig.CACHE_BUCKET);
    var appender = LogUtils.getTestingAppenderForRootLogger();

    attempt(
            () ->
                s3Driver.insertFile(
                    UnixPath.of(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT), csv))
        .orElseThrow();
    csvLoader = ChannelRegistryCsvLoader.load(s3Client);
    var cacheEntries = csvLoader.getCacheEntries();

    assertThat(cacheEntries.size(), is(equalTo(1)));
    assertThat(appender.getMessages(), containsString("Failed to parse 4 out of 5 CSV lines"));
  }
}
