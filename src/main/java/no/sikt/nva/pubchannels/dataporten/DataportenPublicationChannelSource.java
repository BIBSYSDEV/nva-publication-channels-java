package no.sikt.nva.pubchannels.dataporten;

import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import no.sikt.nva.pubchannels.handler.PublicationChannelSource;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataportenPublicationChannelSource implements PublicationChannelSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataportenPublicationChannelSource.class);

    private static final String ENV_DATAPORTEN_CHANNEL_REGISTRY_BASE_URL = "DATAPORTEN_CHANNEL_REGISTRY_BASE_URL";

    private final transient HttpClient httpClient;
    private final transient URI dataportenBaseUri;

    public DataportenPublicationChannelSource(HttpClient httpClient, URI dataportenBaseUri) {
        this.httpClient = httpClient;
        this.dataportenBaseUri = dataportenBaseUri;
    }

    @JacocoGenerated // only used when running on AWS
    public static PublicationChannelSource defaultInstance() {
        var environment = new Environment();
        var baseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_CHANNEL_REGISTRY_BASE_URL));
        return new DataportenPublicationChannelSource(HttpClient.newBuilder().build(), baseUri);
    }

    @Override
    public ThirdPartyJournal getJournal(String identifier, String year) throws ApiGatewayException {
        var request = createFetchJournalRequest(identifier, year);

        return executeRequest(request, DataportenJournal.class);
    }

    private <T> T executeRequest(HttpRequest request, Class<T> clazz) throws ApiGatewayException {
        try {
            var response = httpClient.send(request, BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                return attempt(() -> JsonUtils.dtoObjectMapper.readValue(response.body(), clazz)).orElseThrow();
            } else if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new NotFoundException("Journal not found!");
            } else {
                LOGGER.error("Error fetching journal: {} {}", response.statusCode(), response.body());
                throw new BadGatewayException(String.format("Unexpected response from upstream! Got status code %d.",
                                                            response.statusCode()));
            }
        } catch (IOException | InterruptedException e) {
            throw logAndCreateBadRequestException(request.uri(), e);
        }
    }

    private BadGatewayException logAndCreateBadRequestException(URI uri, Exception e) {
        LOGGER.error("Unable to reach upstream: " + uri, e);
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
                   .header("Accept", "application/json")
                   .uri(uri)
                   .GET()
                   .build();
    }
}
