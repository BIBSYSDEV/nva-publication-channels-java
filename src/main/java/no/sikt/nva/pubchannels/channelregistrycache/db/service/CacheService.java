package no.sikt.nva.pubchannels.channelregistrycache.db.service;

import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheEntry;
import no.sikt.nva.pubchannels.channelregistrycache.db.model.ChannelRegistryCacheDao;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.s3.S3Client;

public class CacheService implements PublicationChannelFetchClient {

    private final DynamoDbTable<ChannelRegistryCacheDao> table;

    public CacheService(DynamoDbEnhancedClient client) {
        this.table = client.table(new Environment().readEnv("TABLE_NAME"),
                                  TableSchema.fromImmutableClass(ChannelRegistryCacheDao.class));
    }

    public void loadCacheEntries(S3Client s3Client)

    public void save(ChannelRegistryCacheEntry entry) {
        table.putItem(entry.toDao());
    }

    @Override
    public ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
        throws ApiGatewayException {
        var channel = table.getItem(ChannelRegistryCacheDao.builder().identifier(UUID.fromString(identifier)).build());
        return ChannelRegistryCacheEntry.fromDao(channel).toThirdPartyPublicationChannel(type, year);
    }
}
