package no.sikt.nva.pubchannels.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.pubchannels.channelRegistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelRegistry.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.channelRegistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelRegistry.model.search.ChannelRegistryEntityPageInformation;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.core.SingletonCollector;
import nva.commons.core.paths.UriWrapper;

public class TestUtils {

    public static final String PAGERESULT_FIELD = "pageresult";
    public static final int YEAR_START = 1900;
    public static final String VALID_ISBN_PREFIX = "12345-1234567";
    private static final ScientificValueMapper mapper = new ScientificValueMapper();

    private TestUtils() {

    }

    public static URI createExpectedUri(String pid, String channelPathElement) {
        return new UriWrapper(HTTPS, "localhost")
                   .addChild("publication-channels", channelPathElement, pid, currentYear())
                   .getUri();
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

    public static void mockRedirectedClient(String requestedIdentifier, String location, String year,
                                            String path) {
        stubFor(
            get(path + requestedIdentifier + "/" + year)
                .withHeader(ACCEPT, equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_MOVED_PERM)
                        .withHeader("Location", location)));
    }

    public static int randomYear() {
        var bound = (LocalDate.now().getYear() + 1) - YEAR_START;
        return YEAR_START + randomInteger(bound);
    }

    public static InputStream constructRequest(String year, String identifier, MediaType mediaType)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper)
                   .withHeaders(Map.of(ACCEPT, mediaType.toString()))
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

    public static String validIsbnPrefix() {
        return VALID_ISBN_PREFIX;
    }

    public static String randomLevel() {
        return String.valueOf((int) Math.floor(Math.random() * (MAX_LEVEL - MIN_LEVEL + 1) + MIN_LEVEL));
    }

    public static List<String> getDataportenSearchResult(int year, String name, int maxNr) {
        return IntStream.range(0, maxNr)
                   .mapToObj(
                       i -> createDataportenJournalResponse(year, name, UUID.randomUUID().toString(), randomIssn(),
                                                            randomIssn(), randomUri(), randomLevel()))
                   .collect(Collectors.toList());
    }

    public static List<String> getDataportenSearchPublisherResult(int year, String name, int maxNr) {
        return IntStream.range(0, maxNr)
                   .mapToObj(i -> createDataportenPublisherResponse(year, name, UUID.randomUUID().toString(),
                                                                    String.valueOf(validIsbnPrefix()),
                                                                    randomUri(), randomLevel()))
                   .collect(Collectors.toList());
    }

    public static ThirdPartyJournal createJournal(
        String year,
        String identifier,
        String name,
        String electronicIssn,
        String issn,
        ScientificValue scientificValue,
        URI landingPage,
        String discontinued) {

        return new ThirdPartyJournal() {
            @Override
            public String identifier() {
                return identifier;
            }

            @Override
            public String getYear() {
                return year;
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
            public String discontinued() {
                return discontinued;
            }

            @Override
            public String onlineIssn() {
                return electronicIssn;
            }

            @Override
            public String printIssn() {
                return issn;
            }
        };
    }

    public static ThirdPartySeries createSeries(
        String year,
        String identifier,
        String name,
        String electronicIssn,
        String issn,
        ScientificValue scientificValue,
        URI landingPage,
        String discontinued) {

        return new ThirdPartySeries() {
            @Override
            public String identifier() {
                return identifier;
            }

            @Override
            public String getYear() {
                return year;
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
            public String discontinued() {
                return discontinued;
            }

            @Override
            public String onlineIssn() {
                return electronicIssn;
            }

            @Override
            public String printIssn() {
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
        URI landingPage,
        String discontinued) {

        return new ThirdPartyPublisher() {
            @Override
            public String identifier() {
                return identifier;
            }

            @Override
            public String getYear() {
                return year;
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
            public String discontinued() {
                return discontinued;
            }

            @Override
            public String isbnPrefix() {
                return isbnPrefix;
            }
        };
    }

    public static ChannelRegistryClient setupInterruptedClient() throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        when(httpClient.send(any(), any())).thenThrow(new InterruptedException());
        var dataportenBaseUri = URI.create("https://localhost:9898");

        return new ChannelRegistryClient(httpClient,
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

    public static String createDataportenJournalResponse(Integer year, String originalTitle, String pid, String eissn,
                                                         String pissn, URI kurl, String level) {
        return new ChannelRegistryBodyBuilder()
                   .withPid(pid)
                   .withOriginalTitle(originalTitle)
                   .withEissn(eissn)
                   .withPissn(pissn)
                   .withKurl(kurl.toString())
                   .withLevel(new ChannelRegistryLevel(year, level))
                   .build();
    }

    public static String createDataportenPublisherResponse(Integer year, String name, String pid, String isbnPrefix,
                                                           URI kurl, String level) {
        return new ChannelRegistryBodyBuilder()
                   .withPid(pid)
                   .withName(name)
                   .withIsbnPrefix(isbnPrefix)
                   .withKurl(kurl.toString())
                   .withLevel(new ChannelRegistryLevel(year, level))
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

    public static String getDataportenResponseBody(List<String> results, int offset, int size) {
        var resultsWithOffsetAndSize =
            results.stream()
                .skip(offset)
                .limit(size)
                .map(result -> attempt(() -> objectMapper.readTree(result)).orElseThrow())
                .collect(Collectors.toList());
        var entityResult = createEntityResultObjectNode(resultsWithOffsetAndSize);
        return buildDataportenSearchResponse(results, entityResult);
    }

    public static String constructExpectedLocation(String newIdentifier, String year, String channelPath) {
        return UriWrapper.fromHost(LOCALHOST)
                   .addChild(CUSTOM_DOMAIN_BASE_PATH, channelPath, newIdentifier, year)
                   .toString();
    }

    private static String currentYear() {
        return Year.now().toString();
    }

    private static String buildDataportenSearchResponse(List<String> results, ObjectNode entityResult) {
        return new ChannelRegistryBodyBuilder()
                   .withEntityPageInformation(new ChannelRegistryEntityPageInformation(results.size()))
                   .withEntityResultSet(entityResult)
                   .build();
    }

    private static ObjectNode createEntityResultObjectNode(List<JsonNode> results) {
        var entityResult = objectMapper.createObjectNode();
        var arrayNode = objectMapper.createArrayNode();
        results.forEach(arrayNode::add);
        entityResult.put(PAGERESULT_FIELD, arrayNode);
        return entityResult;
    }
}
