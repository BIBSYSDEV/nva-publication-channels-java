package no.sikt.nva.pubchannels.channelregistrycache;

import static nva.commons.core.attempt.Try.attempt;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.StringReader;
import java.util.List;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.services.s3.S3Client;

public final class ChannelRegistryCsvCacheClient implements PublicationChannelFetchClient {

    public static final String CHANNEL_NOT_FOUND_MESSAGE = "Could not find cached publication channel with id %s and type %s";
    private final List<ChannelRegistryCacheEntry> cacheEntries;

    private ChannelRegistryCsvCacheClient(List<ChannelRegistryCacheEntry> cacheEntries) {
        this.cacheEntries = cacheEntries;
    }

    public static ChannelRegistryCsvCacheClient load(S3Client s3Client) {
        var s3Driver = new S3Driver(s3Client, "CHANNEL_REGISTER_CACHE_BUCKET");
        var value = attempt(
            () -> s3Driver.getFile(UnixPath.of(new Environment().readEnv("CHANNEL_REGISTER_CACHE")))).orElseThrow();
        return new ChannelRegistryCsvCacheClient(getChannelRegistryCacheFromString(value));
    }

    @Override
    public ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year) {
        return cacheEntries.stream()
                   .filter(entry -> entry.getPid().equals(identifier))
                   .findFirst()
                   .map(entry -> entry.toThirdPartyPublicationChannel(type, year))
                   .orElseThrow(() -> throwException(identifier, type));
    }

    private RuntimeException throwException(String identifier, ChannelType type) {
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
