package no.sikt.nva.pubchannels.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE_APPLICATION_JSON;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE_APPLICATION_JSON_UTF8;
import static no.sikt.nva.pubchannels.HttpHeaders.LOCATION;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.channelregistry.model.search.ChannelRegistryEntityPageInformation;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.MediaTypes;
import nva.commons.core.SingletonCollector;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Named;

public final class TestUtils {

  public static final String PAGERESULT_FIELD = "pageresult";
  public static final int YEAR_START = 1900;
  public static final String VALID_ISBN_PREFIX = "12345-1234567";

  private TestUtils() {}

  public static URI createPublicationChannelUri(
      String pid, String channelPathElement, String year) {
    return new UriWrapper(HTTPS, API_DOMAIN)
        .addChild(CUSTOM_DOMAIN_BASE_PATH, channelPathElement, pid, year)
        .getUri();
  }

  public static void mockChannelRegistryResponse(
      String channelRegistryPathElement, String year, String identifier, String responseBody) {
    stubFor(
        get(channelRegistryPathElement + identifier + "/" + year)
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .willReturn(
                aResponse()
                    .withStatus(HttpURLConnection.HTTP_OK)
                    .withHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_UTF8)
                    .withBody(responseBody)));
  }

  public static void mockRedirectedClient(
      String requestedIdentifier, String location, String year, String path) {
    stubFor(
        get(path + requestedIdentifier + "/" + year)
            .withHeader(ACCEPT, equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .willReturn(
                aResponse()
                    .withStatus(HttpURLConnection.HTTP_MOVED_PERM)
                    .withHeader(LOCATION, location)));
  }

  public static int randomYearAsInt() {
    var bound = (LocalDate.now().getYear() + 1) - YEAR_START;
    return YEAR_START + randomInteger(bound);
  }

  public static String randomYear() {
    return String.valueOf(randomYearAsInt());
  }

  public static Stream<String> invalidYearsProvider() {
    return Stream.of(" ", "abcd", "2101");
  }

  public static InputStream constructRequest(
      String year, String identifier, String type, MediaType mediaType)
      throws JsonProcessingException {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withHeaders(Map.of(ACCEPT, mediaType.toString()))
        .withPathParameters(
            Map.of(
                "identifier", identifier,
                "year", year,
                "type", type))
        .build();
  }

  public static InputStream constructRequest(String identifier, String type, MediaType mediaType)
      throws JsonProcessingException {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withHeaders(Map.of(ACCEPT, mediaType.toString()))
        .withPathParameters(
            Map.of(
                "identifier", identifier,
                "type", type))
        .build();
  }

  public static InputStream constructRequest(
      Map<String, String> queryParameters, MediaType mediaType) throws JsonProcessingException {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withHeaders(Map.of(ACCEPT, mediaType.toString()))
        .withQueryParameters(queryParameters)
        .build();
  }

  public static String scientificValueToLevel(ScientificValue scientificValue) {
    return ScientificValueMapper.VALUES.entrySet().stream()
        .filter(item -> item.getValue().equals(scientificValue))
        .map(Map.Entry::getKey)
        .collect(SingletonCollector.collectOrElse(null));
  }

  public static String validIsbnPrefix() {
    return VALID_ISBN_PREFIX;
  }

  public static List<String> getChannelRegistrySearchResult(String year, String name, int maxNr) {
    return IntStream.range(0, maxNr)
        .mapToObj(i -> generateChannelRegistryJournalBody(year, name))
        .toList();
  }

  public static Map<String, StringValuePattern> getStringStringValuePatternHashMap(
      String... queryValue) {
    var queryParams = new HashMap<String, StringValuePattern>();
    for (int i = 0; i < queryValue.length; i = i + 2) {
      if (nonNull(queryValue[i + 1])) {
        queryParams.put(queryValue[i], equalTo(queryValue[i + 1]));
      }
    }
    return queryParams;
  }

  public static ChannelRegistryClient setupInterruptedClient()
      throws IOException, InterruptedException {
    var httpClient = mock(HttpClient.class);
    when(httpClient.send(any(), any())).thenThrow(new InterruptedException());
    var dataportenBaseUri = URI.create("https://localhost:9898");

    return new ChannelRegistryClient(httpClient, dataportenBaseUri, null);
  }

  public static void mockResponseWithHttpStatus(
      String pathParameter, String identifier, String year, int httpStatus) {
    stubFor(
        get(pathParameter + identifier + "/" + year)
            .withHeader("Accept", equalTo("application/json"))
            .willReturn(aResponse().withStatus(httpStatus)));
  }

  public static URI constructPublicationChannelUri(
      String pathElement, Map<String, String> queryParams) {
    var uri =
        new UriWrapper(HTTPS, API_DOMAIN).addChild(CUSTOM_DOMAIN_BASE_PATH, pathElement).getUri();
    if (nonNull(queryParams)) {
      return UriWrapper.fromUri(uri).addQueryParameters(queryParams).getUri();
    }
    return uri;
  }

  public static String getChannelRegistrySearchResponseBody(
      List<String> results, int offset, int size) {
    var resultsWithOffsetAndSize =
        results.stream()
            .skip(offset)
            .limit(size)
            .map(result -> attempt(() -> objectMapper.readTree(result)).orElseThrow())
            .toList();
    var entityResult = createEntityResultObjectNode(resultsWithOffsetAndSize);
    return buildChannelRegistrySearchResponse(results, entityResult);
  }

  /** Compares two URIs for equality, ignoring the order of query parameters. */
  public static boolean areEqualURIs(URI uri1, URI uri2) {
    if (!Objects.equals(uri1.getScheme(), uri2.getScheme())
        || !Objects.equals(uri1.getHost(), uri2.getHost())
        || !Objects.equals(uri1.getPath(), uri2.getPath())) {
      return false;
    }

    var params1 = getQueryParameters(uri1);
    var params2 = getQueryParameters(uri2);

    return params1.equals(params2);
  }

  public static String currentYear() {
    return String.valueOf(LocalDate.now().getYear());
  }

  public static Integer currentYearAsInteger() {
    return Integer.parseInt(currentYear());
  }

  public static Stream<Named<MediaType>> mediaTypeProvider() {
    return Stream.of(
        Named.of("JSON UTF-8", MediaType.JSON_UTF_8),
        Named.of("ANY", MediaType.ANY_TYPE),
        Named.of("JSON-LD", MediaTypes.APPLICATION_JSON_LD));
  }

  private static String generateChannelRegistryJournalBody(String year, String name) {
    return new TestChannel(year, UUID.randomUUID().toString(), JOURNAL_TYPE)
        .withName(name)
        .asChannelRegistrySerialPublicationBody();
  }

  private static Map<String, String> getQueryParameters(URI uri) {
    return uri.getQuery() == null
        ? Map.of()
        : Arrays.stream(uri.getQuery().split("&"))
            .map(param -> param.split("="))
            .collect(
                Collectors.toMap(param -> param[0], param -> param.length > 1 ? param[1] : ""));
  }

  private static String buildChannelRegistrySearchResponse(
      List<String> results, ObjectNode entityResult) {
    return new ChannelRegistrySearchResponseBodyBuilder()
        .withEntityPageInformation(new ChannelRegistryEntityPageInformation(results.size()))
        .withEntityResultSet(entityResult)
        .build();
  }

  private static ObjectNode createEntityResultObjectNode(List<JsonNode> results) {
    var entityResult = objectMapper.createObjectNode();
    var arrayNode = objectMapper.createArrayNode();
    results.forEach(arrayNode::add);
    entityResult.set(PAGERESULT_FIELD, arrayNode);
    return entityResult;
  }
}
