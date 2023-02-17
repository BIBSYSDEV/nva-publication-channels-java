package no.sikt.nva.pubchannels.dataporten;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

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

public class DataportenPublicationChannelClient implements PublicationChannelClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataportenPublicationChannelClient.class);

    private static final String ENV_DATAPORTEN_CHANNEL_REGISTRY_BASE_URL = "DATAPORTEN_CHANNEL_REGISTRY_BASE_URL";
    public static final String APPLICATION_JSON = "application/json";
    private final transient HttpClient httpClient;
    private final transient URI dataportenBaseUri;
    private final transient AuthClient authClient;

    @JacocoGenerated
    public DataportenPublicationChannelClient(URI uri, AuthClient authClient) {
        this(HttpClient.newBuilder().build(), uri, authClient);
    }

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
    public ThirdPartyJournal getJournal(String identifier, String year) throws ApiGatewayException {
        var request = createFetchJournalRequest(identifier, year);
        return executeRequest(request, DataportenJournal.class);
    }

    @Override
    public String createJournal(String name) throws ApiGatewayException {
        var token = authClient.createToken();
        var request = createCreateJournalRequest(token, name);
        return executeRequest(request, String.class);
    }

    private <T> T executeRequest(HttpRequest request, Class<T> clazz) throws ApiGatewayException {
        try {
            var response = httpClient.send(request, BodyHandlers.ofString());

            switch (response.statusCode()) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_CREATED:
                    if (clazz == String.class) {
                        return clazz.cast(response.body());
                    } else {
                        return attempt(() -> dtoObjectMapper.readValue(response.body(), clazz)).orElseThrow();
                    }
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new NotFoundException("Journal not found!");
                default:
                    LOGGER.error("Error fetching journal: {} {}", response.statusCode(), response.body());
                    throw new BadGatewayException("Unexpected response from upstream!");
            }
        } catch (IOException | InterruptedException e) {
            throw logAndCreateBadGatewayException(request.uri(), e);
        }
    }

    private BadGatewayException logAndCreateBadGatewayException(URI uri, Exception e) {
        LOGGER.error("Unable to reach upstream: {}", uri, e);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return new BadGatewayException("Unable to reach upstream!");
    }

    private HttpRequest createFetchJournalRequest(String identifier, String year) {
        var uri = UriWrapper.fromUri(dataportenBaseUri)
                      .addChild("findjournal", identifier, year)
                      .getUri();
        return HttpRequest.newBuilder()
                   .header("Accept", APPLICATION_JSON)
                   .uri(uri)
                   .GET()
                   .build();
    }

    private HttpRequest createCreateJournalRequest(String token, String name) {
        var uri = UriWrapper.fromUri(dataportenBaseUri)
                .addChild("createjournal", "createpid")
                .getUri();

        JournalRequestBody journalRequestBody = new JournalRequestBody(name);

        String journalRequestBodyAsString = attempt(() -> dtoObjectMapper.writeValueAsString(journalRequestBody))
                .orElseThrow();

        return HttpRequest.newBuilder()
                .header("Accept", APPLICATION_JSON)
                .header("Content-Type", APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(journalRequestBodyAsString))
                .build();
    }
}
