package no.sikt.nva.pubchannels.handler.cache;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import nva.commons.core.JacocoGenerated;
import software.amazon.awssdk.services.s3.S3Client;

public class LoadCacheHandler implements RequestStreamHandler {

    private final CacheService cacheService;
    private final S3Client s3Client;

    @JacocoGenerated
    public LoadCacheHandler() {
        this.cacheService = CacheService.defaultInstance();
        this.s3Client = S3Client.create();
    }

    public LoadCacheHandler(CacheService cacheService, S3Client s3Client) {
        this.cacheService = cacheService;
        this.s3Client = s3Client;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        cacheService.loadCache(s3Client);
    }
}
