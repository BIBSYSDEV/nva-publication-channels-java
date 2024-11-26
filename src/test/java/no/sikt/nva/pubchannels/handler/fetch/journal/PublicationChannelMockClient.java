package no.sikt.nva.pubchannels.handler.fetch.journal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;

public class PublicationChannelMockClient {

    public static final String ACCEPT = "Accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CHANNEL_REGISTRY_JOURNAL_PATH = "/findjournal/";
    private static final URI SELF_URI_BASE = URI.create("https://localhost/publication-channels/journal");
    private final Map<String, SerialPublicationDto> journalsByIdentifier = new ConcurrentHashMap<>();

    public SerialPublicationDto getJournal(String identifier) {
        return journalsByIdentifier.get(identifier);
    }

    public String randomJournal(int year) {
        var identifierString = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, identifierString, "Journal");

        mockChannelRegistry(year, testChannel);
        return identifierString;
    }

    public String journalWithScientificValueReviewNotice(int year) {
        var identifierString = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, identifierString, "Journal")
                              .withScientificValueReviewNotice(Map.of("en", "some comment",
                                                                      "no", "vedtak"));

        mockChannelRegistry(year, testChannel);
        return identifierString;
    }

    public void redirect(String requestedIdentifier, String location, String year) {
        stubFor(
            get(CHANNEL_REGISTRY_JOURNAL_PATH + requestedIdentifier + "/" + year)
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_MOVED_PERM)
                        .withHeader("Location", location)));
    }

    public void mockChannelRegistry(int year, TestChannel testChannel) {
        mockChannelRegistry(year, testChannel, testChannel.asChannelRegistryJournalBody());
    }

    public void mockChannelRegistry(int year, TestChannel testChannel, String channelRegistryJournalBody) {
        mockChannelRegistry(year, testChannel.getIdentifier(), channelRegistryJournalBody);
        journalsByIdentifier.put(testChannel.getIdentifier(),
                                 testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year)));
    }

    private static void mockChannelRegistry(int year, String identifier, String responseBody) {
        stubFor(
            get(CHANNEL_REGISTRY_JOURNAL_PATH + identifier + "/" + year)
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(responseBody)));
    }
}
