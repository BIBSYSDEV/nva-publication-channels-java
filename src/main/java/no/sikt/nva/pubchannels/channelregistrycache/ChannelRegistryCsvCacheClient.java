package no.sikt.nva.pubchannels.channelregistrycache;

import static nva.commons.core.attempt.Try.attempt;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.services.s3.S3Client;

public final class ChannelRegistryCsvCacheClient {

    private ChannelRegistryCsvCacheClient() {
    }

    public static ChannelRegistryCsvCacheClient load(S3Client s3Client) {
        var s3Driver = new S3Driver(s3Client, new Environment().readEnv("CHANNEL_REGISTER_CACHE_BUCKET"));
        var value = attempt(() -> s3Driver.listAllFiles(UnixPath.of("")));
        return
    }

    private static List<ChannelRegistryCacheEntry> getChannelRegistryCacheEntries() throws FileNotFoundException {
        return new CsvToBeanBuilder<ChannelRegistryCacheEntry>(new FileReader(TEST_CSV)).withType(
                ChannelRegistryCacheEntry.class)
                   .withSeparator(';')
                   .withIgnoreEmptyLine(true)
                   .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                   .build()
                   .parse();
    }
}
