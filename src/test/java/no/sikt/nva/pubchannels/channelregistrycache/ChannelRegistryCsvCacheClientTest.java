package no.sikt.nva.pubchannels.channelregistrycache;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.net.URI;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySeries;
import no.sikt.nva.pubchannels.handler.fetch.ChannelRegistryCacheSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChannelRegistryCsvCacheClientTest extends ChannelRegistryCacheSetup {

    private ChannelRegistryCsvCacheClient cacheClient;

    @BeforeEach
    void setUp() {
        super.setup();
        cacheClient = ChannelRegistryCsvCacheClient.load(super.getS3Client());
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
    void shouldLoadCachedPublicationChannelWhenJournal() throws CachedPublicationChannelNotFoundException {
        var channelIdentifier = "50561B90-6679-4FCD-BCB0-99E521B18962";
        var year = "2008";

        var journal = cacheClient.getChannel(ChannelType.JOURNAL, channelIdentifier, year);
        var expectedJournal = createExpectedJournal(channelIdentifier, year);

        assertEquals(expectedJournal, journal);
    }

    @Test
    void shouldLoadCachedPublicationChannelWhenSeries() throws CachedPublicationChannelNotFoundException {
        var channelIdentifier = "50561B90-6679-4FCD-BCB0-99E521B18962";
        var year = "2008";

        var journal = cacheClient.getChannel(ChannelType.SERIES, channelIdentifier, year);
        var expectedJournal = createExpectedSeries(channelIdentifier, year);

        assertEquals(expectedJournal, journal);
    }

    @Test
    void shouldLoadCachedPublicationChannelWhenPublisher() throws CachedPublicationChannelNotFoundException {
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
}