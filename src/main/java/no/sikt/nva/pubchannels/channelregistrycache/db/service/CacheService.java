package no.sikt.nva.pubchannels.channelregistrycache.db.service;

import static nva.commons.core.attempt.Try.attempt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import no.sikt.nva.pubchannels.channelregistrycache.CachedPublicationChannelNotFoundException;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheEntry;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCsvLoader;
import no.sikt.nva.pubchannels.channelregistrycache.db.model.ChannelRegistryCacheDao;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.fetch.RequestObject;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.s3.S3Client;

public class CacheService implements PublicationChannelFetchClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);
  private static final int BATCH_SIZE = 25;
  private final DynamoDbTable<ChannelRegistryCacheDao> table;
  private final DynamoDbEnhancedClient client;

  public CacheService(DynamoDbEnhancedClient client) {
    this.client = client;
    this.table =
        client.table(
            new Environment().readEnv("TABLE_NAME"),
            TableSchema.fromImmutableClass(ChannelRegistryCacheDao.class));
  }

  @JacocoGenerated
  public static CacheService defaultInstance() {
    return new CacheService(DynamoDbEnhancedClient.builder().build());
  }

  public void loadCache(S3Client s3Client) {
    var loader = new ChannelRegistryCsvLoader(s3Client);
    var result = loader.getEntries();

    var counter = new AtomicInteger(0);
    var batchCounter = new AtomicInteger(0);
    var batch = new ArrayList<ChannelRegistryCacheDao>(BATCH_SIZE);
    var seenPids = new ConcurrentHashMap<UUID, Boolean>();
    var batchLock = new ReentrantLock();

    result
        .entries()
        .filter(entry -> seenPids.putIfAbsent(entry.getPid(), Boolean.TRUE) == null)
        .map(ChannelRegistryCacheEntry::toDao)
        .forEach(
            dao -> {
              counter.incrementAndGet();
              batchLock.lock();
              try {
                batch.add(dao);
                if (batch.size() == BATCH_SIZE) {
                  writeBatch(batch);
                  int totalProcessed = batchCounter.addAndGet(BATCH_SIZE);
                  if (totalProcessed % 2000 == 0) {
                    LOGGER.info("Loaded {} entries", totalProcessed);
                  }
                  batch.clear();
                }
              } finally {
                batchLock.unlock();
              }
            });

    if (!batch.isEmpty()) {
      writeBatch(batch);
      LOGGER.info("Loaded {} entries", batchCounter.addAndGet(batch.size()));
    }

    LOGGER.info("Cache loaded with {} entries", counter.get());
    LOGGER.info(result.report().get());
  }

  private void writeBatch(List<ChannelRegistryCacheDao> batch) {
    var writeBatch = WriteBatch.builder(ChannelRegistryCacheDao.class).mappedTableResource(table);
    batch.forEach(writeBatch::addPutItem);
    client.batchWriteItem(r -> r.addWriteBatch(writeBatch.build()));
  }

  public void save(ChannelRegistryCacheEntry entry) {
    table.putItem(entry.toDao());
  }

  @Override
  public ThirdPartyPublicationChannel getChannel(RequestObject requestObject)
      throws CachedPublicationChannelNotFoundException {
    return attempt(requestObject::identifier)
        .map(CacheService::entryWithIdentifier)
        .map(table::getItem)
        .map(ChannelRegistryCacheEntry::fromDao)
        .map(entry -> entry.toThirdPartyPublicationChannel(requestObject))
        .orElseThrow(
            failure -> new CachedPublicationChannelNotFoundException(requestObject.identifier()));
  }

  private static ChannelRegistryCacheDao entryWithIdentifier(String identifier) {
    return ChannelRegistryCacheDao.builder().identifier(UUID.fromString(identifier)).build();
  }
}
