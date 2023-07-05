package no.sikt.nva.pubchannels.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.sikt.nva.pubchannels.TestCommons.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestCommons.LOCALHOST;
import static no.sikt.nva.pubchannels.TestCommons.MAX_LEVEL;
import static no.sikt.nva.pubchannels.TestCommons.MIN_LEVEL;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.dataporten.model.search.DataPortenEntityPageInformation;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyJournal;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublisher;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.core.SingletonCollector;
import nva.commons.core.paths.UriWrapper;

public class TestUtils {

    private static final int YEAR_START = 1900;
    private static final ScientificValueMapper mapper = new ScientificValueMapper();

    private TestUtils() {

    }

    public static ScientificValue getScientificValue(String level) {
        return mapper.map(level);
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

    //TODO: Check isbn prefix format with PO
    public static int randomIsbnPrefix() {
        int min = 1000;
        int max = 9999;
        return new Random().nextInt(max - min + 1) + min;
    }

    public static String randomLevel() {
        return String.valueOf((int) Math.floor(Math.random() * (MAX_LEVEL - MIN_LEVEL + 1) + MIN_LEVEL));
    }

    public static List<String> getDataportenSearchResult(String year, String name, int maxNr) {
        return IntStream.range(0, maxNr)
                   .mapToObj(i -> createDataportenJournalResult(year, name, UUID.randomUUID().toString(), randomIssn(),
                                                                randomIssn(), randomUri(), randomLevel()))
                   .collect(Collectors.toList());
    }

    public static List<String> getDataportenSearchPublisherResult(String year, String name, int maxNr) {
        return IntStream.range(0, maxNr)
                   .mapToObj(i -> createDataportenPublisherResult(year, name, UUID.randomUUID().toString(),
                                                                  String.valueOf(randomIsbnPrefix()),
                                                                  randomUri(), randomLevel()))
                   .collect(Collectors.toList());
    }

    public static ThirdPartyJournal createChannel(
        String year,
        String identifier,
        String name,
        String electronicIssn,
        String issn,
        ScientificValue scientificValue,
        URI landingPage) {

        return new ThirdPartyJournal() {
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
            public ScientificValue getScientificValue() {
                return scientificValue;
            }

            @Override
            public URI getHomepage() {
                return landingPage;
            }

            @Override
            public String getOnlineIssn() {
                return electronicIssn;
            }

            @Override
            public String getPrintIssn() {
                return issn;
            }
        };
    }

    public static ThirdPartyPublisher createPublisher(
        String year,
        String identifier,
        String name,
        String isbnPrefix,
        ScientificValue scientificValue,
        URI landingPage) {

        return new ThirdPartyPublisher() {
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
            public ScientificValue getScientificValue() {
                return scientificValue;
            }

            @Override
            public URI getHomepage() {
                return landingPage;
            }

            @Override
            public String getIsbnPrefix() {
                return isbnPrefix;
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

    public static String getResponseBody(
        String year,
        String identifier,
        String name,
        String isbnPrefix,
        String level,
        URI landingPage,
        String type) {

        return new DataportenBodyBuilder()
                   .withType(type)
                   .withYear(year)
                   .withPid(identifier)
                   .withName(name)
                   .withIsbnPrefix(isbnPrefix)
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

    public static String createDataportenJournalResult(String year, String name, String pid, String eissn, String pissn,
                                                       URI kurl, String level) {
        return new DataportenBodyBuilder().withYear(year)
                   .withPid(pid)
                   .withName(name)
                   .withEissn(eissn)
                   .withPissn(pissn)
                   .withKurl(kurl.toString())
                   .withLevel(level)
                   .build();
    }

    public static String createDataportenPublisherResult(String year, String name, String pid, String isbnPrefix,
                                                         URI kurl, String level) {
        return new DataportenBodyBuilder().withYear(year)
                   .withPid(pid)
                   .withName(name)
                   .withIsbnPrefix(isbnPrefix)
                   .withKurl(kurl.toString())
                   .withLevel(level)
                   .build();
    }

    public static URI constructPublicationChannelUri(String pathElement, Map<String, String> queryParams) {
        var uri = new UriWrapper(HTTPS, LOCALHOST)
                      .addChild(CUSTOM_DOMAIN_BASE_PATH, pathElement)
                      .getUri();
        if (Objects.nonNull(queryParams)) {
            return UriWrapper.fromUri(uri).addQueryParameters(queryParams).getUri();
        }
        return uri;
    }

    public static String getDataportenResponseBody(List<String> results, int offset,
                                                   int size) {

        var resultsWithOffsetAndSize =
            results.stream()
                .skip(offset)
                .limit(size)
                .map(result -> attempt(() -> objectMapper.readTree(result)).orElseThrow())
                .collect(Collectors.toList());
        var entityResult = objectMapper.createObjectNode();
        var arrayNode = objectMapper.createArrayNode();
        resultsWithOffsetAndSize.forEach(arrayNode::add);
        entityResult.put("pageresult", arrayNode);

        return new DataportenBodyBuilder()
                   .withEntityPageInformation(new DataPortenEntityPageInformation(results.size()))
                   .withEntityResultSet(entityResult)
                   .build();
    }
}
