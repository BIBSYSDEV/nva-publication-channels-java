package no.sikt.nva.pubchannels.handler.fetch;

import static nva.commons.core.attempt.Try.attempt;
import java.nio.file.Path;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCacheConfig;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCsvCacheClient;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;

public class ChannelRegistryCacheSetup {

    private ChannelRegistryCsvCacheClient cacheClient;

    public ChannelRegistryCsvCacheClient getCacheClient() {
        return cacheClient;
    }

    public void setup() {
        var csv = IoUtils.stringFromResources(Path.of("cache.csv"));
        var s3Client = new FakeS3Client();
        var s3Driver = new S3Driver(s3Client, ChannelRegistryCacheConfig.CACHE_BUCKET);
        attempt(() -> s3Driver.insertFile(UnixPath.of(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT),
                                          csv)).orElseThrow();
        this.cacheClient = ChannelRegistryCsvCacheClient.load(s3Client);
    }

    public String getCachedJournalSeriesIdentifier() {
        return "50561B90-6679-4FCD-BCB0-99E521B18962";
    }

    public String getCachedJournalSeriesYear() {
        return "2024";
    }

    public String getCachedPublisherIdentifier() {
        return "09D6F92E-B0F6-4B62-90AB-1B9E767E9E11";
    }
}
