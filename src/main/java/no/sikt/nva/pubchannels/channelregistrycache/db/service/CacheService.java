package no.sikt.nva.pubchannels.channelregistrycache.db.service;

import static nva.commons.core.attempt.Try.attempt;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistrycache.CachedPublicationChannelNotFoundException;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheEntry;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCsvLoader;
import no.sikt.nva.pubchannels.channelregistrycache.db.model.ChannelRegistryCacheDao;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
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
        this.table = client.table(new Environment().readEnv("TABLE_NAME"),
                                  TableSchema.fromImmutableClass(ChannelRegistryCacheDao.class));
    }

    @JacocoGenerated
    public static CacheService defaultInstance() {
        return new CacheService(DynamoDbEnhancedClient.builder().build());
    }

    public void loadCache(S3Client s3Client) {
        var entries = ChannelRegistryCsvLoader.load(s3Client).getCacheEntries();
        int start = 0;
        while (start < entries.size()) {
            var writeBatchBuilder = WriteBatch.builder(ChannelRegistryCacheDao.class).mappedTableResource(table);
            entries.subList(start, Math.min(start + BATCH_SIZE, entries.size()))
                .stream()
                .map(ChannelRegistryCacheEntry::toDao)
                .forEach(writeBatchBuilder::addPutItem);
            var writeBatch = writeBatchBuilder.build();
            client.batchWriteItem(r -> r.addWriteBatch(writeBatch));
            start += BATCH_SIZE;
        }
        LOGGER.info("Cache loaded with {} entries", entries.size());
    }

    public void save(ChannelRegistryCacheEntry entry) {
        table.putItem(entry.toDao());
    }

    @Override
    public ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
        throws CachedPublicationChannelNotFoundException {
        return attempt(() -> UUID.fromString(identifier))
                   .map(CacheService::entryWithIdentifier)
                   .map(table::getItem)
                   .map(ChannelRegistryCacheEntry::fromDao)
                   .map(entry -> entry.toThirdPartyPublicationChannel(type, year))
                   .orElseThrow(failure -> new CachedPublicationChannelNotFoundException(identifier));
    }

    private static ChannelRegistryCacheDao entryWithIdentifier(UUID uuid) {
        return ChannelRegistryCacheDao.builder().identifier(uuid).build();
    }
}
