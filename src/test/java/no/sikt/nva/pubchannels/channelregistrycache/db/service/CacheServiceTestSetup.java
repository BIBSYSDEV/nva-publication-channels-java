package no.sikt.nva.pubchannels.channelregistrycache.db.service;

import static no.sikt.nva.pubchannels.channelregistrycache.db.model.ChannelRegistryCacheDao.PRIMARY_KEY;
import static no.sikt.nva.pubchannels.channelregistrycache.db.model.ChannelRegistryCacheDao.SORT_KEY;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import java.nio.file.Path;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheConfig;
import no.sikt.nva.pubchannels.utils.AppConfig;
import no.sikt.nva.pubchannels.utils.FakeAppConfig;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public class CacheServiceTestSetup {

    private DynamoDbClient client;

    public void setup() {
        this.client = DynamoDBEmbedded.create().dynamoDbClient();
        createTable(new Environment().readEnv("TABLE_NAME"));
    }

    public AppConfig getAppConfigWithCacheEnabled(boolean cacheEnabled) {
        return new FakeAppConfig(cacheEnabled);
    }

    public void loadAndEnableCache() {
        var csv = IoUtils.stringFromResources(Path.of("cache.csv"));
        var s3Client = new FakeS3Client();
        var s3Driver = new S3Driver(s3Client, ChannelRegistryCacheConfig.CACHE_BUCKET);
        attempt(() -> s3Driver.insertFile(UnixPath.of(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT),
                                          csv)).orElseThrow();
        new CacheService(getClient()).loadCache(s3Client);
    }

    public DynamoDbEnhancedClient getClient() {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    public void createTable(String tableName) {
        var request = CreateTableRequest.builder()
                          .attributeDefinitions(AttributeDefinition.builder()
                                                    .attributeName(PRIMARY_KEY)
                                                    .attributeType(ScalarAttributeType.S) // UUID as String
                                                    .build(), AttributeDefinition.builder()
                                                                  .attributeName(SORT_KEY)
                                                                  .attributeType(
                                                                      ScalarAttributeType.S) // type as String
                                                                  .build())
                          .keySchema(KeySchemaElement.builder()
                                         .attributeName(PRIMARY_KEY)
                                         .keyType(KeyType.HASH) // Partition key
                                         .build(), KeySchemaElement.builder()
                                                       .attributeName(SORT_KEY)
                                                       .keyType(KeyType.RANGE) // Sort key
                                                       .build())
                          .provisionedThroughput(
                              ProvisionedThroughput.builder().readCapacityUnits(10L).writeCapacityUnits(10L).build())
                          .tableName(tableName)
                          .build();

        client.createTable(request);
    }
}
