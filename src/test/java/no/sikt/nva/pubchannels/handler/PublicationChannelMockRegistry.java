package no.sikt.nva.pubchannels.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import no.sikt.nva.pubchannels.model.JournalDto;

public class PublicationChannelMockRegistry {

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

    public String randomJournal(String year) {
        var identifier = UUID.randomUUID().toString();
        var name = randomString();
        var electronicIssn = randomIssn();
        var issn = randomIssn();
        var scientificValue = randomElement(ScientificValue.values());
        var landingPage = randomUri();

        mockDataporten(year, identifier, name, electronicIssn, issn, scientificValue, landingPage);

        URI selfUriBase = URI.create("https://localhost/publication-channels/journal");
        ThirdPartyJournal journal = new ThirdPartyJournal() {
            @Override
            public String getIdentifier() {
                return identifier;
            }

            @Override
            public String getYear() {
                return year;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getOnlineIssn() {
                return electronicIssn;
            }

            @Override
            public String getPrintIssn() {
                return issn;
            }

            @Override
            public ScientificValue getScientificValue() {
                return scientificValue;
            }

            @Override
            public URI getHomepage() {
                return landingPage;
            }
        };

        var journalDto = JournalDto.create(selfUriBase, journal);

        journalsByIdentifier.put(identifier, journalDto);

        return identifier;
    }

    private void mockDataporten(String year,
                                String identifier,
                                String name,
                                String electronicIssn,
                                String issn,
                                ScientificValue scientificValue,
                                URI landingPage) {

        var level = scientificValueToLevel(scientificValue);
        var body = new DataportenBodyBuilder()
                       .withYear(year)
                       .withPid(identifier)
                       .withName(name)
                       .withEissn(electronicIssn)
                       .withPissn(issn)
                       .withLevel(level)
                       .withKurl(landingPage.toString())
                       .build();

        stubFor(
            get("/findjournal/" + identifier + "/" + year)
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(body)));
    }

    private String scientificValueToLevel(ScientificValue scientificValue) {
        switch (scientificValue) {
            case LEVEL_ZERO:
                return "0";
            case LEVEL_ONE:
                return "1";
            case LEVEL_TWO:
                return "2";
            case UNASSIGNED:
            default:
                return null;
        }
    }
}
