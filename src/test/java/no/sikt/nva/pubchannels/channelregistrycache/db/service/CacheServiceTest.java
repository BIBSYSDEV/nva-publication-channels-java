package no.sikt.nva.pubchannels.channelregistrycache.db.service;

import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheEntry;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

class CacheServiceTest extends CacheServiceDynamoDbSetup {

    private CacheService cacheService;

    @BeforeEach
    public void setup() {
        super.setup();
        var client = DynamoDbEnhancedClient.builder().dynamoDbClient(super.getClient()).build();
        cacheService = new CacheService(client);
    }

    @Test
    void shouldStoreCacheEntry() throws ApiGatewayException {
        var channel = ChannelRegistryCacheEntry.builder()
                        .withPid(UUID.randomUUID().toString())
                        .withIsbn(randomString())
                        .withUri(randomUri().toString())
                        .build();

        cacheService.save(channel);

        var year = String.valueOf(randomYear());
        var persistedChannel = cacheService.getChannel(ChannelType.JOURNAL, channel.getPid(), year);

        assertEquals(channel.toThirdPartyPublicationChannel(ChannelType.JOURNAL, year), persistedChannel);
    }
}