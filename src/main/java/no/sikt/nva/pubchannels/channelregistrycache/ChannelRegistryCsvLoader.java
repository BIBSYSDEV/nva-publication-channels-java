package no.sikt.nva.pubchannels.channelregistrycache;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.StringReader;
import java.util.List;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public final class ChannelRegistryCsvLoader {

    private final List<ChannelRegistryCacheEntry> cacheEntries;

    private ChannelRegistryCsvLoader(List<ChannelRegistryCacheEntry> cacheEntries) {
        this.cacheEntries = cacheEntries;
    }

    public static ChannelRegistryCsvLoader load(S3Client s3Client) {
        var value = s3Client.getObject(getCacheRequest(), ResponseTransformer.toBytes()).asUtf8String();
        return new ChannelRegistryCsvLoader(getChannelRegistryCacheFromString(value));
    }

    public List<ChannelRegistryCacheEntry> getCacheEntries() {
        return cacheEntries;
    }

    private static GetObjectRequest getCacheRequest() {
        return GetObjectRequest.builder()
                   .bucket(ChannelRegistryCacheConfig.CACHE_BUCKET)
                   .key(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT)
                   .build();
    }

    private static List<ChannelRegistryCacheEntry> getChannelRegistryCacheFromString(String value) {
        return new CsvToBeanBuilder<ChannelRegistryCacheEntry>(new StringReader(value)).withType(
                ChannelRegistryCacheEntry.class)
                   .withSeparator(';')
                   .withIgnoreEmptyLine(true)
                   .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                   .build()
                   .parse();
    }
}
