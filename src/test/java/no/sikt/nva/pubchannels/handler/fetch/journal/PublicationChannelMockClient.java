package no.sikt.nva.pubchannels.handler.fetch.journal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import no.sikt.nva.pubchannels.channelRegistry.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.channelRegistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.handler.ChannelRegistryBodyBuilder;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.unit.nva.testutils.RandomDataGenerator;
import nva.commons.core.SingletonCollector;

public class PublicationChannelMockClient {

    public static final String ACCEPT = "Accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CHANNEL_REGISTRY_JOURNAL_PATH = "/findjournal/";
    private final Map<String, FetchByIdAndYearResponse> journalsByIdentifier = new ConcurrentHashMap<>();

    public void notFoundJournal(String identifier, String year) {
        mockJournalNotFound(identifier, year);
    }

    public void internalServerErrorJournal(String identifier, String year) {
        mockJournalInternalServerError(identifier, year);
    }

    public FetchByIdAndYearResponse getJournal(String identifier) {
        return journalsByIdentifier.get(identifier);
    }

    public String randomJournal(int year) {
        var identifier = UUID.randomUUID().toString();
        var name = randomString();
        var electronicIssn = randomIssn();
        var issn = randomIssn();
        var scientificValue = RandomDataGenerator.randomElement(ScientificValue.values());
        var landingPage = randomUri();
        var discontinued = String.valueOf(year - 1);

        var responseBody = getDataportenResponseBody(year, identifier, name, electronicIssn, issn, scientificValue,
                                                     landingPage, discontinued);

        mockDataporten(year, identifier, responseBody);

        createJournalAndAddToMap(year, identifier, name, electronicIssn, issn, scientificValue, landingPage, discontinued);

        return identifier;
    }

    public String randomJournalWithThirdPartyYearValueNull(int year) {
        var identifier = UUID.randomUUID().toString();
        var name = randomString();
        var electronicIssn = randomIssn();
        var issn = randomIssn();
        var scientificValue = RandomDataGenerator.randomElement(ScientificValue.values());
        var landingPage = randomUri();
        var discontinued = String.valueOf(year - 1);

        var responseBody = getDataportenResponseBody(null, identifier, name, electronicIssn, issn, scientificValue,
                                                     landingPage, discontinued);

        mockDataporten(year, identifier, responseBody);
        createJournalAndAddToMap(year, identifier, name, electronicIssn, issn, scientificValue, landingPage,
                                 discontinued);
        return identifier;
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

    private static void mockDataporten(int year, String identifier, String responseBody) {
        stubFor(
            get(CHANNEL_REGISTRY_JOURNAL_PATH + identifier + "/" + year)
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(responseBody)));
    }

    private void createJournalAndAddToMap(Integer year, String identifier, String name, String electronicIssn,
                                          String issn,
                                          ScientificValue scientificValue, URI landingPage, String discontinued) {
        var selfUriBase = URI.create("https://localhost/publication-channels/journal");
        var journal = new ThirdPartyJournal() {
            @Override
            public String identifier() {
                return identifier;
            }

            @Override
            public String getYear() {
                return String.valueOf(year);
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public ScientificValue getScientificValue() {
                return scientificValue;
            }

            @Override
            public URI homepage() {
                return landingPage;
            }

            @Override
            public String onlineIssn() {
                return electronicIssn;
            }

            @Override
            public String printIssn() {
                return issn;
            }

            @Override
            public String discontinued() {
                return discontinued;
            }
        };

        var journalDto = FetchByIdAndYearResponse.create(selfUriBase, journal, String.valueOf(year));

        journalsByIdentifier.put(identifier, journalDto);
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

    private String getDataportenResponseBody(Integer year,
                                             String identifier,
                                             String name,
                                             String electronicIssn,
                                             String issn,
                                             ScientificValue scientificValue,
                                             URI landingPage, String discontinued) {

        var level = scientificValueToLevel(scientificValue);
        return new ChannelRegistryBodyBuilder()
                   .withType("Journal")
                   .withPid(identifier)
                   .withOriginalTitle(name)
                   .withEissn(electronicIssn)
                   .withPissn(issn)
                   .withLevel(new ChannelRegistryLevel(year, level))
                   .withKurl(landingPage.toString())
                   .withCeased(discontinued)
                   .build();
    }

    private String scientificValueToLevel(ScientificValue scientificValue) {

        return ScientificValueMapper.VALUES.entrySet()
                   .stream()
                   .filter(item -> item.getValue().equals(scientificValue))
                   .map(Entry::getKey)
                   .collect(SingletonCollector.collectOrElse(null));
    }
}
