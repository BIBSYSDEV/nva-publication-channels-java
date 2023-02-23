package no.sikt.nva.pubchannels.dataporten;

import no.sikt.nva.pubchannels.dataporten.model.CreateJournalRequest;
import no.sikt.nva.pubchannels.dataporten.model.CreateJournalResponse;
import no.sikt.nva.pubchannels.handler.AuthClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Set;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
import static no.sikt.nva.pubchannels.HttpHeaders.AUTHORIZATION;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE_APPLICATION_JSON;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

public class DataportenPublicationChannelClient implements PublicationChannelClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataportenPublicationChannelClient.class);

    private static final String ENV_DATAPORTEN_CHANNEL_REGISTRY_BASE_URL = "DATAPORTEN_CHANNEL_REGISTRY_BASE_URL";
    private static final Set<Integer> OK_STATUSES = Set.of(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);
    private static final String FETCH_JOURNAL_PATH = "findjournal";
    private final HttpClient httpClient;
    private final URI dataportenBaseUri;
    private final AuthClient authClient;

    public DataportenPublicationChannelClient(HttpClient httpClient, URI dataportenBaseUri, AuthClient authClient) {
        this.httpClient = httpClient;
        this.dataportenBaseUri = dataportenBaseUri;
        this.authClient = authClient;
    }

    @JacocoGenerated // only used when running on AWS
    public static PublicationChannelClient defaultInstance() {
        var environment = new Environment();
        var baseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_CHANNEL_REGISTRY_BASE_URL));
        return new DataportenPublicationChannelClient(HttpClient.newBuilder().build(), baseUri, null);
    }

    @Override
    public ThirdPartyJournal getJournalByIdentifierAndYear(String identifier, String year) throws ApiGatewayException {
        var request = createFetchRequest(FETCH_JOURNAL_PATH, identifier, year);
        return attempt(() -> executeRequest(request, DataportenJournal.class))
                .orElseThrow(failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
    }

    @Override
    public FetchJournalByIdentifierDto getJournalByIdentifier(String identifier) {
        var request = createFetchRequest(FETCH_JOURNAL_PATH, identifier);
        return attempt(() -> executeRequest(request, FetchJournalByIdentifierDto.class)).orElseThrow();
    }

    @Override
    public CreateJournalResponse createJournal(String name) throws ApiGatewayException {
        var token = authClient.getToken();
        var request = createCreateJournalRequest(token, name);
        return attempt(() -> executeRequest(request, CreateJournalResponse.class))
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
        if (HTTP_NOT_FOUND == response.statusCode()) {
            throw new NotFoundException("Journal not found!");
        }
        LOGGER.error("Error fetching journal: {} {}", response.statusCode(), response.body());
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

    private HttpRequest createFetchRequest(String... children) {
        return HttpRequest.newBuilder()
                .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                .uri(constructUri(children))
                .GET()
                .build();
    }

    private HttpRequest createCreateJournalRequest(String token, String name) {

        var journalRequestBodyAsString =
                attempt(() -> dtoObjectMapper.writeValueAsString(new CreateJournalRequest(name)))
                        .orElseThrow();

        return HttpRequest.newBuilder()
                .header(ACCEPT, CONTENT_TYPE_APPLICATION_JSON)
                .header(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + token)
                .uri(constructUri("createjournal", "createpid"))
                .POST(HttpRequest.BodyPublishers.ofString(journalRequestBodyAsString))
                .build();
    }

    private URI constructUri(String... children) {
        return UriWrapper.fromUri(dataportenBaseUri)
                .addChild(children)
                .getUri();
    }
}
