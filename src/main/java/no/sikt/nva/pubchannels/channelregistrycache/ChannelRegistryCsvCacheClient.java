package no.sikt.nva.pubchannels.channelregistrycache;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.StringReader;
import java.util.List;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public final class ChannelRegistryCsvCacheClient implements PublicationChannelFetchClient {

    public static final String CHANNEL_NOT_FOUND_MESSAGE = "Could not find cached publication channel with id %s and type %s";
    private final List<ChannelRegistryCacheEntry> cacheEntries;

    private ChannelRegistryCsvCacheClient(List<ChannelRegistryCacheEntry> cacheEntries) {
        this.cacheEntries = cacheEntries;
    }

    public static ChannelRegistryCsvCacheClient load(S3Client s3Client) {
        var value = s3Client.getObject(getCacheRequest(), ResponseTransformer.toBytes()).asUtf8String();
        return new ChannelRegistryCsvCacheClient(getChannelRegistryCacheFromString(value));
    }

    private static GetObjectRequest getCacheRequest() {
        return GetObjectRequest.builder()
                   .bucket(ChannelRegistryCacheConfig.CACHE_BUCKET)
                   .key(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT)
                   .build();
    }

    @Override
    public ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
        throws CachedPublicationChannelNotFoundException {
        return cacheEntries.stream()
                   .filter(entry -> entry.getPid().equals(identifier))
                   .findFirst()
                   .map(entry -> entry.toThirdPartyPublicationChannel(type, year))
                   .orElseThrow(() -> throwException(identifier, type));
    }

    private CachedPublicationChannelNotFoundException throwException(String identifier, ChannelType type) {
        return new CachedPublicationChannelNotFoundException(CHANNEL_NOT_FOUND_MESSAGE.formatted(identifier, type.name()));
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
