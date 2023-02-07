package no.sikt.nva.pubchannels.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.sikt.nva.pubchannels.model.Contexts.PUBLICATION_CHANNEL_CONTEXT;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import no.sikt.nva.pubchannels.model.Journal;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.testutils.RandomDataGenerator;
import nva.commons.core.paths.UriWrapper;

public class PublicationChannelMockRegistry {

    public void notFoundJournal(String identifier, String year) {
        mockJournalNotFound(identifier, year);
    }

    public void internalServerErrorJournal(String identifier, String year) {
        mockJournalInternalServerError(identifier, year);
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

    public Journal randomJournal(String year) throws JsonProcessingException {
        var context = URI.create(PUBLICATION_CHANNEL_CONTEXT);
        var identifier = UUID.randomUUID().toString();
        var id = UriWrapper.fromUri("https://localhost")
                     .addChild("publication-channels", "journal", identifier, year)
                     .getUri();
        var name = randomString();
        var electronicIssn = randomIssn();
        var issn = randomIssn();
        var level = RandomDataGenerator.randomElement("0", "1", "2");
        var landingPage = randomUri();

        mockDataporten(year, identifier, name, electronicIssn, issn, level, landingPage);

        return new Journal(context, id, identifier, year, name, electronicIssn, issn, level, landingPage);
    }

    private void mockDataporten(String year,
                                String identifier,
                                String name,
                                String electronicIssn,
                                String issn,
                                String level,
                                URI landingPage)
        throws JsonProcessingException {

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
        body.put("Level", level);
        body.put("KURL", landingPage.toString());

        return JsonUtils.dtoObjectMapper.writeValueAsString(body);
    }
}
