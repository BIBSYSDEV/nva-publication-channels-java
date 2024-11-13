package no.sikt.nva.pubchannels.channelregistrycache;

import no.unit.nva.stubs.FakeS3Client;
import org.junit.jupiter.api.Test;

class ChannelRegistryCsvCacheClientTest {

    @Test
    void shouldLoadCacheFromS3Bucket() {
        var s3Client = new FakeS3Client();
        var cacheClient = ChannelRegistryCsvCacheClient.load(s3Client);
    }
}