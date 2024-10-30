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
import no.sikt.nva.pubchannels.handler.model.JournalDto;

public class PublicationChannelMockClient {

    public static final String ACCEPT = "Accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CHANNEL_REGISTRY_JOURNAL_PATH = "/findjournal/";
    private static final URI SELF_URI_BASE = URI.create("https://localhost/publication-channels/journal");
    private final Map<String, JournalDto> journalsByIdentifier = new ConcurrentHashMap<>();

    public void notFoundJournal(String identifier, String year) {
        mockJournalNotFound(identifier, year);
    }

    public void internalServerErrorJournal(String identifier, String year) {
        mockJournalInternalServerError(identifier, year);
    }

    public JournalDto getJournal(String identifier) {
        return journalsByIdentifier.get(identifier);
    }

    public String randomJournal(int year) {
        var identifierString = UUID.randomUUID().toString();
        var testData = new TestChannel(year, identifierString);

        mockChannelRegistry(year, identifierString, testData.asChannelRegistryJournalBody());
        journalsByIdentifier.put(identifierString, testData.asJournalDto(SELF_URI_BASE, String.valueOf(year)));

        return identifierString;
    }

    public String randomJournalWithThirdPartyYearValueNull(int year) {
        var identifierString = UUID.randomUUID().toString();
        var testData = new TestChannel(null, identifierString);

        mockChannelRegistry(year, identifierString, testData.asChannelRegistryJournalBody());
        journalsByIdentifier.put(identifierString, testData.asJournalDto(SELF_URI_BASE, String.valueOf(year)));

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

    private void mockJournalNotFound(String identifier, String year) {
        stubFor(
            get("/findjournal/" + identifier + "/" + year)
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_NOT_FOUND)));
    }

    private void mockJournalInternalServerError(String identifier, String year) {
        stubFor(
            get("/findjournal/" + identifier + "/" + year)
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)));
    }
}
