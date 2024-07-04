package no.sikt.nva.pubchannels.channelRegistry;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
import static no.sikt.nva.pubchannels.HttpHeaders.AUTHORIZATION;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE_APPLICATION_JSON;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Set;
import no.sikt.nva.pubchannels.channelRegistry.model.create.ChannelRegistryCreateJournalRequest;
import no.sikt.nva.pubchannels.channelRegistry.model.create.ChannelRegistryCreatePublisherRequest;
import no.sikt.nva.pubchannels.channelRegistry.model.create.ChannelRegistryCreateSeriesRequest;
import no.sikt.nva.pubchannels.channelRegistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.handler.AuthClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelRegistryClient implements PublicationChannelClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelRegistryClient.class);
    private static final String ENV_CHANNEL_REGISTRY_BASE_URL = "DATAPORTEN_CHANNEL_REGISTRY_BASE_URL";
    private static final Set<Integer> OK_STATUSES = Set.of(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);
    private static final String SEARCH_PATH_ELEMENT = "channels";
    private final HttpClient httpClient;
    private final URI channelRegistryBaseUri;
    private final AuthClient authClient;

    public ChannelRegistryClient(HttpClient httpClient, URI channelRegistryBaseUri, AuthClient authClient) {
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

    @Override
    public ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
        throws ApiGatewayException {
        var request = createFetchPublicationChannelRequest(type.pathElement, identifier, year);
        return attempt(() -> executeRequest(request, type.fetchResponseClass)).orElseThrow(
            failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
    }

    @Override
    public ThirdPartySearchResponse searchChannel(ChannelType type, Map<String, String> queryParameters)
        throws ApiGatewayException {
        var request = createFindPublicationChannelRequest(type.pathElement, queryParameters);
        return attempt(() -> executeRequest(request, type.searchResponseClass))
                   .orElseThrow(failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
    }

    @Override
    public CreateChannelResponse createJournal(ChannelRegistryCreateJournalRequest body)
        throws ApiGatewayException {
        var token = authClient.getToken();
        var request = createCreateJournalRequest(token, body);
        return attempt(() -> executeRequest(request, CreateChannelResponse.class))
                   .orElseThrow(failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
    }

    @Override
    public CreateChannelResponse createPublisher(ChannelRegistryCreatePublisherRequest body)
        throws ApiGatewayException {
        var token = authClient.getToken();
        var request = createCreatePublisherRequest(token, body);
        return attempt(() -> executeRequest(request, CreateChannelResponse.class))
                   .orElseThrow(failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
    }

    @Override
    public CreateChannelResponse createSeries(ChannelRegistryCreateSeriesRequest body) throws ApiGatewayException {
        var token = authClient.getToken();
        var request = createCreateSeriesRequest(token, body);
        return attempt(() -> executeRequest(request, CreateChannelResponse.class))
                   .orElseThrow(failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
    }

    private <T> T executeRequest(HttpRequest request, Class<T> clazz)
        throws ApiGatewayException, IOException, InterruptedException {
        var response = httpClient.send(request, BodyHandlers.ofString());

        if (!OK_STATUSES.contains(response.statusCode())) {
            handleError(response);
        }

        return attempt(() -> dtoObjectMapper.readValue(response.body(), clazz)).orElseThrow();
    }

    private void handleError(HttpResponse<String> response) throws ApiGatewayException {
        var statusCode = response.statusCode();
        if (HTTP_NOT_FOUND == statusCode) {
            throw new NotFoundException("Publication channel not found!");
        }
        if (HTTP_BAD_REQUEST == statusCode) {
            throw new BadRequestException(response.body());
        }
        if (HTTP_MOVED_PERM == statusCode) {
            var location = response.headers().map().get("Location").get(0);
            LOGGER.info("Publication channel moved permanently to: {}", location);
            throw new PublicationChannelMovedException("Publication channel moved permanently!", URI.create(location));
        }
        LOGGER.error("Error fetching publication channel: {} {}", statusCode, response.body());
        throw new BadGatewayException("Unexpected response from upstream!");
    }

    private ApiGatewayException logAndCreateBadGatewayException(URI uri, Exception e) {
        LOGGER.error("Unable to reach upstream: {}", uri, e);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else if (e instanceof ApiGatewayException) {
            return (ApiGatewayException) e;
        }
        return new BadGatewayException("Unable to reach upstream!");
    }

    private HttpRequest createFetchPublicationChannelRequest(String pathElement, String identifier, String year) {
        return HttpRequest.newBuilder()
                   .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                   .uri(constructUri(pathElement, identifier, year))
                   .GET()
                   .build();
    }

    private HttpRequest createFindPublicationChannelRequest(String pathElement, Map<String, String> queryParams) {
        return HttpRequest.newBuilder()
                   .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                   .uri(addQueryParamameters(constructUri(pathElement, SEARCH_PATH_ELEMENT), queryParams))
                   .GET()
                   .build();
    }

    private HttpRequest createCreateJournalRequest(String token, ChannelRegistryCreateJournalRequest body) {

        var bodyAsJsonString =
            attempt(() -> dtoObjectMapper.writeValueAsString(body))
                .orElseThrow();

        return getHttpRequest(token, bodyAsJsonString, "createjournal");
    }

    private HttpRequest createCreatePublisherRequest(String token, ChannelRegistryCreatePublisherRequest request) {
        var bodyAsJsonString =
            attempt(() -> dtoObjectMapper.writeValueAsString(request))
                .orElseThrow();

        return getHttpRequest(token, bodyAsJsonString, "createpublisher");
    }

    private HttpRequest createCreateSeriesRequest(String token, ChannelRegistryCreateSeriesRequest body) {

        var bodyAsJsonString =
            attempt(() -> dtoObjectMapper.writeValueAsString(body))
                .orElseThrow();

        return getHttpRequest(token, bodyAsJsonString, "createseries");
    }

    private HttpRequest getHttpRequest(String token, String journalRequestBodyAsString, String path) {
        return HttpRequest.newBuilder()
                   .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                   .header(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                   .header(AUTHORIZATION, "Bearer " + token)
                   .uri(constructUri(path, "createpid"))
                   .POST(HttpRequest.BodyPublishers.ofString(journalRequestBodyAsString))
                   .build();
    }

    private URI constructUri(String... children) {
        return UriWrapper.fromUri(channelRegistryBaseUri)
                   .addChild(children)
                   .getUri();
    }

    private URI addQueryParamameters(URI uri, Map<String, String> queryParameters) {
        return UriWrapper.fromUri(uri)
                   .addQueryParameters(queryParameters)
                   .getUri();
    }
}
