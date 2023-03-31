package no.sikt.nva.pubchannels.handler.fetch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.DataportenBodyBuilder;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.core.SingletonCollector;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FetchByIdAndYearTestUtil {

    private static final int YEAR_START = 1900;

    private FetchByIdAndYearTestUtil() {

    }

    public static void mockDataportenResponse(String dataportenPathElement, String year, String identifier,
                                              String responseBody) {
        stubFor(
                get(dataportenPathElement + identifier + "/" + year)
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpURLConnection.HTTP_OK)
                                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                                        .withBody(responseBody)));
    }

    public static String randomYear() {
        var bound = (LocalDate.now().getYear() + 1) - YEAR_START;
        return Integer.toString(YEAR_START + randomInteger(bound));
    }

    public static InputStream constructRequest(String year, String identifier) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper)
                .withPathParameters(Map.of(
                        "identifier", identifier,
                        "year", year
                ))
                .build();
    }

    public static String scientificValueToLevel(ScientificValue scientificValue) {

        return ScientificValueMapper.VALUES.entrySet()
                .stream()
                .filter(item -> item.getValue().equals(scientificValue))
                .map(Map.Entry::getKey)
                .collect(SingletonCollector.collectOrElse(null));
    }

    public static ThirdPartyPublicationChannel getChannel(
            String year,
            String identifier,
            String name,
            String electronicIssn,
            String issn,
            ScientificValue scientificValue,
            URI landingPage) {

        return new ThirdPartyPublicationChannel() {
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
    }

    public static String getResponseBody(
            String year,
            String identifier,
            String name,
            String electronicIssn,
            String issn,
            String level,
            URI landingPage,
            String type) {

        return new DataportenBodyBuilder()
                .withType(type)
                .withYear(year)
                .withPid(identifier)
                .withName(name)
                .withEissn(electronicIssn)
                .withPissn(issn)
                .withLevel(level)
                .withKurl(landingPage.toString())
                .build();
    }

    public static DataportenPublicationChannelClient setupInterruptedClient() throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        when(httpClient.send(any(), any())).thenThrow(new InterruptedException());
        var dataportenBaseUri = URI.create("https://localhost:9898");

        return new DataportenPublicationChannelClient(httpClient,
                dataportenBaseUri, null);
    }

    public static void mockResponseWithHttpStatus(String pathParameter, String identifier, String year,
                                                  int httpStatus) {
        stubFor(
                get(pathParameter + identifier + "/" + year)
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(httpStatus)));
    }
}
