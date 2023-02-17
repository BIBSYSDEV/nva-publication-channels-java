package no.sikt.nva.pubchannels.dataporten;

import no.sikt.nva.pubchannels.handler.AuthClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

public class DataportenAuthClient implements AuthClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataportenAuthClient.class);
    private final HttpClient httpClient;
    private final URI baseUri;
    private final String clientId;
    private final String clientSecret;

    public DataportenAuthClient(URI baseUri, String clientId, String clientSecret) {
        this(HttpClient.newBuilder().build(), baseUri, clientId, clientSecret);
    }

    public DataportenAuthClient(HttpClient httpClient, URI baseUri, String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = httpClient;
        this.baseUri = baseUri;
    }


    @Override
    public String createToken() throws ApiGatewayException {
        var request = createTokenRequest(clientId, clientSecret);
        var tokenBody = executeRequest(request, TokenBody.class);
        return tokenBody.getAccessToken();
    }

    private HttpRequest createTokenRequest(String clientId, String clientSecret) {
        var uri = UriWrapper.fromUri(baseUri)
                .addChild("oauth", "token")
                .getUri();
        var credentials = clientId + ":" + clientSecret;
        byte[] encodedCredentials = Base64.getEncoder().encode(credentials.getBytes());
        return HttpRequest.newBuilder()
                .header("Content-Type", "x-www-form-urlencoded")
                .header("Authorization", String.format("Basic %s", new String(encodedCredentials)))
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials", StandardCharsets.UTF_8))
                .build();
    }

    private <T> T executeRequest(HttpRequest request, Class<T> clazz) throws ApiGatewayException {
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                return attempt(() -> dtoObjectMapper.readValue(response.body(), clazz)).orElseThrow();
            }
            LOGGER.error("Error excecuting request: {} {} {}", request.uri(), response.statusCode(), response.body());
            throw new BadGatewayException("Unexpected response from upstream!");
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
}
