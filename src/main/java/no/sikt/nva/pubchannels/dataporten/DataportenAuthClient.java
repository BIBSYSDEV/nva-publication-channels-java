package no.sikt.nva.pubchannels.dataporten;

import no.sikt.nva.pubchannels.HttpHeaders;
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

    public DataportenAuthClient(HttpClient httpClient, URI baseUri, String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = httpClient;
        this.baseUri = baseUri;
    }


    @Override
    public String getToken() throws ApiGatewayException {
        var request = createTokenRequest(clientId, clientSecret);
        var tokenBody = fetchToken(request);
        return tokenBody.getAccessToken();
    }

    private HttpRequest createTokenRequest(String clientId, String clientSecret) {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_X_WWW_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, getCredentialsString(clientId, clientSecret))
                .uri(getTokenRequestUri())
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials", StandardCharsets.UTF_8))
                .build();
    }

    private String getCredentialsString(String clientId, String clientSecret) {
        var credentials = clientId + ":" + clientSecret;
        var encodedCredentials = Base64.getEncoder().encode(credentials.getBytes());
        return String.format("Basic %s", new String(encodedCredentials));
    }

    private URI getTokenRequestUri() {
        return UriWrapper.fromUri(baseUri)
                .addChild("oauth", "token")
                .getUri();
    }

    private TokenBody fetchToken(HttpRequest request) throws ApiGatewayException {
        return attempt(() -> executeRequest(request))
                .orElseThrow(failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
    }

    private TokenBody executeRequest(HttpRequest request)
            throws IOException, InterruptedException, BadGatewayException {
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            reportFailingRequest(request, response);
        }
        return attempt(() -> dtoObjectMapper.readValue(response.body(), TokenBody.class)).orElseThrow();
    }

    private static void reportFailingRequest(HttpRequest request, HttpResponse<String> response)
            throws BadGatewayException {
        LOGGER.error("Error executing request: {} {} {}", request.uri(), response.statusCode(), response.body());
        throw new BadGatewayException("Unexpected response from upstream!");
    }

    private ApiGatewayException logAndCreateBadGatewayException(URI uri, Exception e) {

        LOGGER.error("Unable to reach upstream: {}", uri, e);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else if (e instanceof BadGatewayException) {
            return new BadGatewayException(e.getMessage());
        }
        return new BadGatewayException("Unable to reach upstream!");
    }

}
