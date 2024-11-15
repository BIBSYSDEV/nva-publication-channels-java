package no.sikt.nva.pubchannels.channelregistrycache.db.service;

import static no.sikt.nva.pubchannels.channelregistrycache.db.model.ChannelRegistryCacheDao.PRIMARY_KEY;
import static no.sikt.nva.pubchannels.channelregistrycache.db.model.ChannelRegistryCacheDao.SORT_KEY;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import nva.commons.core.Environment;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public class CacheServiceDynamoDbSetup {

    private DynamoDbClient client;

    public void setup(){
        this.client = DynamoDBEmbedded.create().dynamoDbClient();
        createTable(new Environment().readEnv("TABLE_NAME"));
    }

    public DynamoDbClient getClient() {
        return client;
    }

    public void createTable(String tableName) {
        CreateTableRequest request = CreateTableRequest.builder()
                                         .attributeDefinitions(
                                             AttributeDefinition.builder()
                                                 .attributeName(PRIMARY_KEY)
                                                 .attributeType(ScalarAttributeType.S) // UUID as String
                                                 .build(),
                                             AttributeDefinition.builder()
                                                 .attributeName(SORT_KEY)
                                                 .attributeType(ScalarAttributeType.S) // type as String
                                                 .build())
                                         .keySchema(
                                             KeySchemaElement.builder()
                                                 .attributeName(PRIMARY_KEY)
                                                 .keyType(KeyType.HASH) // Partition key
                                                 .build(),
                                             KeySchemaElement.builder()
                                                 .attributeName(SORT_KEY)
                                                 .keyType(KeyType.RANGE) // Sort key
                                                 .build())
                                         .provisionedThroughput(
                                             ProvisionedThroughput.builder()
                                                 .readCapacityUnits(10L)
                                                 .writeCapacityUnits(10L)
                                                 .build())
                                         .tableName(tableName)
                                         .build();

        client.createTable(request);
    }
}
