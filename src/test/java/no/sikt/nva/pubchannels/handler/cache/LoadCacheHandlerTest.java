package no.sikt.nva.pubchannels.handler.cache;

import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.io.IOException;
import java.nio.file.Path;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheConfig;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheServiceDynamoDbSetup;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.Test;

class LoadCacheHandlerTest extends CacheServiceDynamoDbSetup {

    public static final String CHANNEL_ID_FROM_CSV = "1013C82D-B452-43E4-9396-FA958F2BC2E9";

    @Test
    void shouldLoadCache() throws IOException {
        super.setup();
        var s3Client = insertCacheCsvToS3();
        var cacheService = new CacheService(super.getClient());
        var handler = getLoadCacheHandler(cacheService, s3Client);

        handler.handleRequest(null, null, new FakeContext());

        assertDoesNotThrow(() -> cacheService.getChannel(ChannelType.JOURNAL, CHANNEL_ID_FROM_CSV,
                                                     String.valueOf(randomYear())));
    }

    private static LoadCacheHandler getLoadCacheHandler(CacheService cacheService, FakeS3Client s3Client) {
        return new LoadCacheHandler(cacheService, s3Client);
    }

    private static FakeS3Client insertCacheCsvToS3() {
        var csv = IoUtils.stringFromResources(Path.of("cache.csv"));
        var s3Client = new FakeS3Client();
        var s3Driver = new S3Driver(s3Client, ChannelRegistryCacheConfig.CACHE_BUCKET);
        attempt(() -> s3Driver.insertFile(UnixPath.of(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT),
                                          csv)).orElseThrow();
        return s3Client;
    }
}