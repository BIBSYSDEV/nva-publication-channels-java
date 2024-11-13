package no.sikt.nva.pubchannels.channelregistrycache;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySeries;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

class ChannelRegistryCsvCacheClientTest {

    public static final String BUCKET_NAME = "someBucket";
    public static final String FILE_NAME = "someFile";

    private ChannelRegistryCsvCacheClient cacheClient;

    @BeforeEach
    void setUp() throws IOException {
        var s3Client = new FakeS3Client();
        initiateS3BucketWithCacheFile(s3Client);
        this.cacheClient = ChannelRegistryCsvCacheClient.load(s3Client);
    }

    @Test
    void shouldThrowExceptionWhenCannotFindPublicationChannel() {
        var channelIdentifier = randomString();
        var year = "2008";

        assertThrows(CachedPublicationChannelNotFoundException.class,
                     () -> cacheClient.getChannel(ChannelType.JOURNAL, channelIdentifier, year),
                     "Could not find cached publication channel with id %s and type %s".formatted(channelIdentifier,
                                                                                                    "JOURNAL"));
    }

    @Test
    void shouldLoadCachedPublicationChannelWhenJournal() {
        var channelIdentifier = "50561B90-6679-4FCD-BCB0-99E521B18962";
        var year = "2008";

        var journal = cacheClient.getChannel(ChannelType.JOURNAL, channelIdentifier, year);
        var expectedJournal = createExpectedJournal(channelIdentifier, year);

        assertEquals(expectedJournal, journal);
    }

    @Test
    void shouldLoadCachedPublicationChannelWhenSeries() {
        var channelIdentifier = "50561B90-6679-4FCD-BCB0-99E521B18962";
        var year = "2008";

        var journal = cacheClient.getChannel(ChannelType.SERIES, channelIdentifier, year);
        var expectedJournal = createExpectedSeries(channelIdentifier, year);

        assertEquals(expectedJournal, journal);
    }

    @Test
    void shouldLoadCachedPublicationChannelWhenPublisher() {
        var channelIdentifier = "09D6F92E-B0F6-4B62-90AB-1B9E767E9E11";
        var year = "2024";

        var journal = cacheClient.getChannel(ChannelType.PUBLISHER, channelIdentifier, year);
        var expectedJournal = createExpectedPublisher(channelIdentifier, year);

        assertEquals(expectedJournal, journal);
    }

    private static ChannelRegistryJournal createExpectedJournal(String channelIdentifier, String year) {
        return new ChannelRegistryJournal(channelIdentifier, "Tidsskrift for Den norske legeforening", "0807-7096",
                                          "0029-2001",
                                          new ChannelRegistryLevel(Integer.parseInt(year), "1", null, null, null),
                                          URI.create(
                                              "https://kanalregister.hkdir.no/publiseringskanaler/KanalTidsskriftInfo" +
                                              "?pid=50561B90-6679-4FCD-BCB0-99E521B18962"), null);
    }

    private ChannelRegistrySeries createExpectedSeries(String channelIdentifier, String year) {
        return new ChannelRegistrySeries(channelIdentifier, "Tidsskrift for Den norske legeforening", "0807-7096",
                                         "0029-2001",
                                         new ChannelRegistryLevel(Integer.parseInt(year), "1", null, null, null),
                                         URI.create(
                                             "https://kanalregister.hkdir.no/publiseringskanaler/KanalTidsskriftInfo" +
                                             "?pid=50561B90-6679-4FCD-BCB0-99E521B18962"), null);
    }

    private ChannelRegistryPublisher createExpectedPublisher(String channelIdentifier, String year) {
        var homepage = "https://kanalregister.hkdir.no/publiseringskanaler/KanalForslagInfo?pid=09D6F92E-B0F6-4B62" +
                       "-90AB-1B9E767E9E11";
        return new ChannelRegistryPublisher(channelIdentifier,
                                            new ChannelRegistryLevel(Integer.parseInt(year), null, null, null, null),
                                            "978-1-9996187", "Agenda Publishing", URI.create(homepage), null);
    }

    private void initiateS3BucketWithCacheFile(S3Client s3Client) throws IOException {
        var csv = IoUtils.stringFromResources(Path.of("cache.csv"));
        new S3Driver(s3Client, BUCKET_NAME).insertFile(UnixPath.of(FILE_NAME), csv);
    }
}