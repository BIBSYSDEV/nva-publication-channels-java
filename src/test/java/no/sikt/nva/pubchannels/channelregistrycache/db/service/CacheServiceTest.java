package no.sikt.nva.pubchannels.channelregistrycache.db.service;

import static no.sikt.nva.pubchannels.TestConstants.HARDCODED_CACHED_TITLE;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySerialPublication;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheConfig;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheEntry;
import no.sikt.nva.pubchannels.handler.fetch.RequestObject;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CacheServiceTest extends CacheServiceTestSetup {

  private CacheService cacheService;

  @Override
  @BeforeEach
  public void setupDynamoDbTable() {
    super.setupDynamoDbTable();
    var client = super.getClient();
    cacheService = new CacheService(client);
  }

  @Test
  void shouldStoreCacheEntry() throws ApiGatewayException {
    var channel =
        ChannelRegistryCacheEntry.builder()
            .withPid(UUID.randomUUID())
            .withIsbn(randomString())
            .withUri(randomUri().toString())
            .build();

    cacheService.save(channel);

    var year = randomYear();
    var requestObject = new RequestObject(ChannelType.JOURNAL, channel.getPid().toString(), year);
    var persistedChannel = cacheService.getChannel(requestObject);

    assertEquals(
        channel.toThirdPartyPublicationChannel(ChannelType.JOURNAL, year), persistedChannel);
  }

  @Test
  void shouldLoadCsvEntriesToDatabase() throws ApiGatewayException {
    var s3Client = s3ClientWithCsvFileInCacheBucket();
    cacheService.loadCache(s3Client);

    var requestObject =
        new RequestObject(ChannelType.JOURNAL, "50561B90-6679-4FCD-BCB0-99E521B18962", "2008");
    var loadedEntry = cacheService.getChannel(requestObject);

    assertNotNull(loadedEntry);
  }

  @Test
  void shouldLoadCachedPublicationChannelWhenJournal() throws ApiGatewayException {
    var s3Client = s3ClientWithCsvFileInCacheBucket();
    cacheService.loadCache(s3Client);
    var channelIdentifier = "50561B90-6679-4FCD-BCB0-99E521B18962";
    var year = "2008";

    var requestObject = new RequestObject(ChannelType.JOURNAL, channelIdentifier, year);
    var journal = cacheService.getChannel(requestObject);
    var expectedJournal = createExpectedJournal(channelIdentifier, year);

    assertEquals(expectedJournal, journal);
  }

  @Test
  void shouldLoadCachedPublicationChannelWhenSeries() throws ApiGatewayException {
    var s3Client = s3ClientWithCsvFileInCacheBucket();
    cacheService.loadCache(s3Client);
    var channelIdentifier = "50561B90-6679-4FCD-BCB0-99E521B18962";
    var year = "2008";

    var requestObject = new RequestObject(ChannelType.SERIES, channelIdentifier, year);
    var journal = cacheService.getChannel(requestObject);
    var expectedJournal = createExpectedSeries(channelIdentifier, year);

    assertEquals(expectedJournal, journal);
  }

  @Test
  void shouldLoadCachedPublicationChannelWhenPublisher() throws ApiGatewayException {
    var s3Client = s3ClientWithCsvFileInCacheBucket();
    cacheService.loadCache(s3Client);
    var channelIdentifier = "09D6F92E-B0F6-4B62-90AB-1B9E767E9E11";
    var year = "2024";

    var requestObject = new RequestObject(ChannelType.PUBLISHER, channelIdentifier, year);
    var journal = (ChannelRegistryPublisher) cacheService.getChannel(requestObject);
    var expectedJournal = createExpectedPublisher(channelIdentifier, year);

    assertEquals(expectedJournal, journal);
  }

  private static ChannelRegistrySerialPublication createExpectedJournal(
      String channelIdentifier, String year) {
    return new ChannelRegistrySerialPublication(
        channelIdentifier,
        HARDCODED_CACHED_TITLE,
        "0807-7096",
        "0029-2001",
        new ChannelRegistryLevel(Integer.parseInt(year), "1", null, null, null),
        URI.create(
            "https://kanalregister.hkdir"
                + ".no/publiseringskanaler/KanalTidsskriftInfo"
                + "?pid=50561B90-6679-4FCD-BCB0-99E521B18962"),
        null,
        "Tidsskrift");
  }

  private static FakeS3Client s3ClientWithCsvFileInCacheBucket() {
    var s3Client = new FakeS3Client();
    var csv = IoUtils.stringFromResources(Path.of("cache.csv"));
    var s3Driver = new S3Driver(s3Client, ChannelRegistryCacheConfig.CACHE_BUCKET);
    attempt(
            () ->
                s3Driver.insertFile(
                    UnixPath.of(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT), csv))
        .orElseThrow();
    return s3Client;
  }

  private ChannelRegistrySerialPublication createExpectedSeries(
      String channelIdentifier, String year) {
    return new ChannelRegistrySerialPublication(
        channelIdentifier,
        HARDCODED_CACHED_TITLE,
        "0807-7096",
        "0029-2001",
        new ChannelRegistryLevel(Integer.parseInt(year), "1", null, null, null),
        URI.create(
            "https://kanalregister.hkdir"
                + ".no/publiseringskanaler/KanalTidsskriftInfo"
                + "?pid=50561B90-6679-4FCD-BCB0-99E521B18962"),
        null,
        "Tidsskrift");
  }

  private ChannelRegistryPublisher createExpectedPublisher(String channelIdentifier, String year) {
    var homepage =
        "https://kanalregister.hkdir.no/publiseringskanaler/KanalForslagInfo?pid=09D6F92E-B0F6-4B62"
            + "-90AB-1B9E767E9E11";
    return new ChannelRegistryPublisher(
        channelIdentifier,
        new ChannelRegistryLevel(Integer.parseInt(year), null, null, null, null),
        "978-1-9996187",
        "Agenda Publishing",
        URI.create(homepage),
        null,
        "publisher");
  }
}
