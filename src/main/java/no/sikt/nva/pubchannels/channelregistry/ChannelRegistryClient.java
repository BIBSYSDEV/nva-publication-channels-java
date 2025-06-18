package no.sikt.nva.pubchannels.channelregistry;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
import static no.sikt.nva.pubchannels.HttpHeaders.AUTHORIZATION;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE_APPLICATION_JSON;
import static no.sikt.nva.pubchannels.channelregistry.ChannelType.PUBLISHER;
import static no.sikt.nva.pubchannels.channelregistry.ChannelType.SERIAL_PUBLICATION;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Year;
import java.util.Map;
import java.util.Set;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreatePublisherRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.handler.AuthClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.fetch.RequestObject;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import nva.commons.secrets.SecretsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelRegistryClient implements PublicationChannelClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChannelRegistryClient.class);
  private static final String ENV_CHANNEL_REGISTRY_BASE_URL =
      "DATAPORTEN_CHANNEL_REGISTRY_BASE_URL";
  private static final Set<Integer> OK_STATUSES =
      Set.of(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);
  private static final String SEARCH_PATH_ELEMENT = "channels";
  protected static final int ONE_HUNDREED = 100;
  protected static final int FOUR = 4;
  protected static final int FIVE = 5;
  private static final String SECRET_NAME = "DataportenChannelRegistryClientCredentials";
  private final HttpClient httpClient;
  private final URI channelRegistryBaseUri;
  private final AuthClient authClient;

  public ChannelRegistryClient(
      HttpClient httpClient, URI channelRegistryBaseUri, AuthClient authClient) {
    this.httpClient = httpClient;
    this.channelRegistryBaseUri = channelRegistryBaseUri;
    this.authClient = authClient;
  }

  @JacocoGenerated // only used when running on AWS
  public static PublicationChannelClient defaultInstance() {
    var environment = new Environment();
    var baseUri = URI.create(environment.readEnv(ENV_CHANNEL_REGISTRY_BASE_URL));
    return new ChannelRegistryClient(HttpClient.newBuilder().build(), baseUri, null);
  }

  @JacocoGenerated // only used when running on AWS
  public static PublicationChannelClient defaultAuthorizedInstance(Environment environment) {
    var secretsReader = new SecretsReader();
    var clientId = secretsReader.fetchSecret(SECRET_NAME, "clientId");
    var clientSecret = secretsReader.fetchSecret(SECRET_NAME, "clientSecret");
    var authBaseUri = URI.create(secretsReader.fetchSecret(SECRET_NAME, "authBaseUrl"));
    var httpClient = HttpClient.newBuilder().build();
    var authClient = new DataportenAuthClient(httpClient, authBaseUri, clientId, clientSecret);
    var baseUri = URI.create(environment.readEnv(ENV_CHANNEL_REGISTRY_BASE_URL));
    return new ChannelRegistryClient(httpClient, baseUri, authClient);
  }

  @Override
  public ThirdPartyPublicationChannel getChannel(RequestObject requestObject)
      throws ApiGatewayException {
    var request = createFetchPublicationChannelRequest(requestObject);
    return attempt(
            () -> executeRequest(request, requestObject.channelType().getFetchResponseClass()))
        .orElseThrow(
            failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
  }

  @Override
  public ThirdPartySearchResponse searchChannel(
      ChannelType type, Map<String, String> queryParameters) throws ApiGatewayException {
    var request =
        createFindPublicationChannelRequest(type.channelRegistryPathElement, queryParameters);
    return attempt(() -> executeRequest(request, type.searchResponseClass))
        .orElseThrow(
            failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
  }

  @Override
  public CreateChannelResponse createJournal(ChannelRegistryCreateSerialPublicationRequest body)
      throws ApiGatewayException {
    var token = authClient.getToken();
    var request = createCreateJournalRequest(token, body);
    return attempt(() -> executeRequest(request, CreateChannelResponse.class))
        .orElseThrow(
            failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
  }

  @Override
  public CreateChannelResponse createPublisher(ChannelRegistryCreatePublisherRequest body)
      throws ApiGatewayException {
    var token = authClient.getToken();
    var request = createCreatePublisherRequest(token, body);
    return attempt(() -> executeRequest(request, CreateChannelResponse.class))
        .orElseThrow(
            failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
  }

  @Override
  public CreateChannelResponse createSeries(ChannelRegistryCreateSerialPublicationRequest body)
      throws ApiGatewayException {
    var token = authClient.getToken();
    var request = createCreateSeriesRequest(token, body);
    return attempt(() -> executeRequest(request, CreateChannelResponse.class))
        .orElseThrow(
            failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
  }

  @Override
  public void updateChannel(ChannelRegistryUpdateChannelRequest request)
      throws ApiGatewayException {
    var token = attempt(authClient::getToken).orElseThrow(failure -> new UnauthorizedException());

    var channel =
        switch (request.type()) {
          case "publisher" ->
              getChannel(new RequestObject(PUBLISHER, request.pid(), Year.now().toString()));
          case "serial-publication" ->
              getChannel(
                  new RequestObject(SERIAL_PUBLICATION, request.pid(), Year.now().toString()));
          default -> throw new BadRequestException("Unsupported channel type: " + request.type());
        };

    if (!channel.getScientificValue().equals(ScientificValue.UNASSIGNED)) {
      throw new BadRequestException(
          "Only channel with unassigned scientific value can be updated!");
    }

    var httpRequest = createChangeChannelRequest(request, token);
    var response =
        attempt(() -> httpClient.send(httpRequest, BodyHandlers.ofString())).orElseThrow();

    if (response.statusCode() / ONE_HUNDREED == FOUR) {
      throw new BadRequestException(response.body());
    }
    if (response.statusCode() / ONE_HUNDREED == FIVE) {
      throw new BadGatewayException("Unexpected response from upstream!");
    }
  }

  private HttpRequest createChangeChannelRequest(
      ChannelRegistryUpdateChannelRequest request, String token) {
    return HttpRequest.newBuilder()
        .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
        .header(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
        .header(AUTHORIZATION, "Bearer " + token)
        .uri(constructUri("admin", "change"))
        .method("PATCH", BodyPublishers.ofString(request.toJsonString()))
        .build();
  }

  private <T> T executeRequest(HttpRequest request, Class<T> clazz)
      throws ApiGatewayException, IOException, InterruptedException {
    var response = httpClient.send(request, BodyHandlers.ofString());

    if (!OK_STATUSES.contains(response.statusCode())) {
      handleError(request.uri(), response);
    }

    return attempt(() -> dtoObjectMapper.readValue(response.body(), clazz)).orElseThrow();
  }

  private void handleError(URI requestedUri, HttpResponse<String> response)
      throws ApiGatewayException {
    var statusCode = response.statusCode();
    if (HTTP_NOT_FOUND == statusCode) {
      LOGGER.info("Publication channel not found: {} {}", requestedUri, response.body());
      throw new NotFoundException("Publication channel not found!");
    }
    if (HTTP_BAD_REQUEST == statusCode) {
      throw new BadRequestException(response.body());
    }
    if (HTTP_MOVED_PERM == statusCode) {
      var location = response.headers().map().get("Location").getFirst();
      LOGGER.info("Publication channel {} moved permanently to: {}", requestedUri, location);
      throw new PublicationChannelMovedException(
          "Publication channel moved permanently!", URI.create(location));
    }
    LOGGER.error(
        "Error fetching publication channel: {} {} {}", requestedUri, statusCode, response.body());
    throw new BadGatewayException("Unexpected response from upstream!");
  }

  @SuppressWarnings("PMD.DoNotUseThreads")
  private ApiGatewayException logAndCreateBadGatewayException(URI uri, Exception e) {
    if (e instanceof InterruptedException) {
      LOGGER.error("Thread interrupted when fetching: {}", uri, e);
      Thread.currentThread().interrupt();
    } else if (e instanceof ApiGatewayException apiGatewayException) {
      return apiGatewayException;
    }
    LOGGER.error("Unable to reach upstream: {}", uri, e);
    return new BadGatewayException("Unable to reach upstream!");
  }

  private HttpRequest createFetchPublicationChannelRequest(RequestObject requestObject) {
    return HttpRequest.newBuilder()
        .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
        .uri(constructUri(requestObject))
        .GET()
        .build();
  }

  private HttpRequest createFindPublicationChannelRequest(
      String pathElement, Map<String, String> queryParams) {
    return HttpRequest.newBuilder()
        .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
        .uri(addQueryParameters(constructUri(pathElement, SEARCH_PATH_ELEMENT), queryParams))
        .GET()
        .build();
  }

  private HttpRequest createCreateJournalRequest(
      String token, ChannelRegistryCreateSerialPublicationRequest body) {

    var bodyAsJsonString = attempt(() -> dtoObjectMapper.writeValueAsString(body)).orElseThrow();

    return getHttpRequest(token, bodyAsJsonString, "createjournal");
  }

  private HttpRequest createCreatePublisherRequest(
      String token, ChannelRegistryCreatePublisherRequest request) {
    var bodyAsJsonString = attempt(() -> dtoObjectMapper.writeValueAsString(request)).orElseThrow();

    return getHttpRequest(token, bodyAsJsonString, "createpublisher");
  }

  private HttpRequest createCreateSeriesRequest(
      String token, ChannelRegistryCreateSerialPublicationRequest body) {

    var bodyAsJsonString = attempt(() -> dtoObjectMapper.writeValueAsString(body)).orElseThrow();

    return getHttpRequest(token, bodyAsJsonString, "createseries");
  }

  private HttpRequest getHttpRequest(String token, String journalRequestBodyAsString, String path) {
    return HttpRequest.newBuilder()
        .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
        .header(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
        .header(AUTHORIZATION, "Bearer " + token)
        .uri(constructUri(path, "createpid"))
        .POST(BodyPublishers.ofString(journalRequestBodyAsString))
        .build();
  }

  private URI constructUri(String... children) {
    return UriWrapper.fromUri(channelRegistryBaseUri).addChild(children).getUri();
  }

  private URI constructUri(RequestObject requestObject) {
    var uriWrapper =
        UriWrapper.fromUri(channelRegistryBaseUri)
            .addChild(requestObject.channelType().getChannelRegistryPathElement())
            .addChild(requestObject.identifier().toString());

    if (requestObject.getYear().isPresent()) {
      return uriWrapper.addChild(requestObject.getYear().get()).getUri();
    }

    return uriWrapper.getUri();
  }

  private URI addQueryParameters(URI uri, Map<String, String> queryParameters) {
    return UriWrapper.fromUri(uri).addQueryParameters(queryParameters).getUri();
  }
}
