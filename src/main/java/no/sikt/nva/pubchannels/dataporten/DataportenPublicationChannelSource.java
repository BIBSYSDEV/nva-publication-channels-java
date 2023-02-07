package no.sikt.nva.pubchannels.dataporten;

import static no.sikt.nva.pubchannels.handler.UrlUtils.JOURNAL_BASE_PATH;
import static no.sikt.nva.pubchannels.model.Contexts.PUBLICATION_CHANNEL_CONTEXT;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.PublicationChannelSource;
import no.sikt.nva.pubchannels.model.Journal;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataportenPublicationChannelSource implements PublicationChannelSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataportenPublicationChannelSource.class);

    private static final String ENV_DATAPORTEN_CHANNEL_REGISTRY_BASE_URL = "DATAPORTEN_CHANNEL_REGISTRY_BASE_URL";
    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";

    private final transient HttpClient httpClient;
    private final transient URI baseUri;
    private final transient URI apiBaseUri;

    public DataportenPublicationChannelSource(HttpClient httpClient, URI baseUri, String apiDomain, String basePath) {
        this.httpClient = httpClient;
        this.baseUri = baseUri;

        this.apiBaseUri = new UriWrapper(HTTPS, apiDomain).addChild(basePath).getUri();
    }

    @JacocoGenerated // only used when running on AWS
    public static PublicationChannelSource defaultInstance() {
        var environment = new Environment();
        var baseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_CHANNEL_REGISTRY_BASE_URL));
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var basePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new DataportenPublicationChannelSource(HttpClient.newBuilder().build(), baseUri, apiDomain, basePath);
    }

    @Override
    public Journal getJournal(String identifier, String year) throws ApiGatewayException {
        var request = createFetchJournalRequest(identifier, year);

        return Optional.of(executeRequest(request, no.sikt.nva.pubchannels.dataporten.Journal.class))
                   .map(journal -> toExternalModel(year, journal))
                   .orElseThrow();
    }

    private Journal toExternalModel(String year, no.sikt.nva.pubchannels.dataporten.Journal localModel) {
        var identifier = localModel.getPid();
        var id = UriWrapper.fromUri(apiBaseUri).addChild(JOURNAL_BASE_PATH, identifier, year).getUri();
        var context = URI.create(PUBLICATION_CHANNEL_CONTEXT);
        return new Journal(context,
                           id,
                           identifier,
                           localModel.getYear(),
                           localModel.getName(),
                           localModel.getEissn(),
                           localModel.getPissn(),
                           localModel.getLevel(),
                           localModel.getKurl()
        );
    }

    private <T> T executeRequest(HttpRequest request, Class<T> clazz) throws ApiGatewayException {
        try {
            var response = httpClient.send(request, BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                return JsonUtils.dtoObjectMapper.readValue(response.body(), clazz);
            } else {
                throw new BadGatewayException(String.format("Unexpected response from upstream! Got status code %d.",
                                                            response.statusCode()));
            }
        } catch (IOException | InterruptedException e) {
            throw logAndCreateBadRequestException(e);
        }
    }

    @JacocoGenerated // InterruptedException hard to trigger in a test
    private BadGatewayException logAndCreateBadRequestException(Exception e) {
        LOGGER.error("Unable to reach upstream!", e);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return new BadGatewayException("Unable to reach upstream!");
    }

    private HttpRequest createFetchJournalRequest(String identifier, String year) {
        var uri = UriWrapper.fromUri(baseUri)
                      .addChild("findjournal", identifier, year)
                      .getUri();
        return HttpRequest.newBuilder()
                   .header("Accept", "application/json")
                   .uri(uri)
                   .GET()
                   .build();
    }
}
