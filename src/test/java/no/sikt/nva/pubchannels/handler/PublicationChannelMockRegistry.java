package no.sikt.nva.pubchannels.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import no.sikt.nva.pubchannels.model.JournalDto;
import no.unit.nva.commons.json.JsonUtils;

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

    public String randomJournal(String year) throws JsonProcessingException {
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
            public URI getLandingPage() {
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
                                URI landingPage)
        throws JsonProcessingException {

        var level = scientificValueToLevel(scientificValue);
        var body = generateBody(year, identifier, name, electronicIssn, issn, level, landingPage);

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

    private String generateBody(String year,
                                String identifier,
                                String name,
                                String electronicIssn,
                                String issn,
                                String level,
                                URI landingPage) throws JsonProcessingException {

        Map<String, Object> body = new ConcurrentHashMap<>();

        body.put("Pid", identifier);
        body.put("type", "Journal");
        body.put("Name", name);
        body.put("Eissn", electronicIssn);
        body.put("Pissn", issn);
        body.put("Year", year);
        if (Objects.nonNull(level)) {
            body.put("Level", level);
        }
        body.put("KURL", landingPage.toString());

        return JsonUtils.dtoObjectMapper.writeValueAsString(body);
    }
}
