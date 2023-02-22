package no.sikt.nva.pubchannels.dataporten;

import no.sikt.nva.pubchannels.handler.AuthClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
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
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE_X_WWW_FORM_URLENCODED = "x-www-form-urlencoded";
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
    public String fetchToken() throws ApiGatewayException {
        var request = createTokenRequest(clientId, clientSecret);
        var tokenBody = fetchToken(request, TokenBody.class);
        return tokenBody.getAccessToken();
    }

    private HttpRequest createTokenRequest(String clientId, String clientSecret) {
        return HttpRequest.newBuilder()
                .header(CONTENT_TYPE, CONTENT_TYPE_X_WWW_FORM_URLENCODED)
                .header(AUTHORIZATION, getCredentialsString(clientId, clientSecret))
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

    private <T> T fetchToken(HttpRequest request, Class<T> clazz) throws ApiGatewayException {
        return attempt(() -> executeRequest(request, clazz))
                .orElseThrow(failure -> logAndCreateBadGatewayException(request.uri(), failure.getException()));
    }

    private <T> T executeRequest(HttpRequest request, Class<T> clazz) throws IOException, InterruptedException, BadGatewayException {
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            reportFailingRequest(request, response);
        }
        return attempt(() -> dtoObjectMapper.readValue(response.body(), clazz)).orElseThrow();
    }

    private static void reportFailingRequest(HttpRequest request, HttpResponse<String> response) throws BadGatewayException {
        LOGGER.error("Error executing request: {} {} {}", request.uri(), response.statusCode(), response.body());
        throw new BadGatewayException("Unexpected response from upstream!");
    }

    // And here it is. I guess I like having the collected handling of exceptions in this way. It makes it easy to throw anywhere and delegate to a single method. It works here because we're throwing ApiGatewayExceptions. Is it pretty? No, but it arguably less ugly than the alternative.
    private ApiGatewayException logAndCreateBadGatewayException(URI uri, Exception e) {

        LOGGER.error("Unable to reach upstream: {}", uri, e);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else if (e instanceof NotFoundException) {
            return (NotFoundException) e;
        } else if (e instanceof BadGatewayException) {
            return new BadGatewayException(e.getMessage());
        }
        return new BadGatewayException("Unable to reach upstream!");
    }

}
