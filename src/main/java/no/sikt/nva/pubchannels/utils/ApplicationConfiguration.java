package no.sikt.nva.pubchannels.utils;

import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

public class ApplicationConfiguration {

    private final static Environment ENVIRONMENT = new Environment();
    public static final String PUBLICATION_CHANNEL_CACHE_ENABLED_CONFIG_PARAM = "publicationChannelCacheEnabled";
    private final HttpClient client;

    public ApplicationConfiguration(HttpClient client) {
        this.client = client;
    }

    @JacocoGenerated
    public static ApplicationConfiguration defaultAppConfigClientInstance() {
        return new ApplicationConfiguration(HttpClient.newHttpClient());
    }

    //TODO: Deprecated getConfiguration method should be removed and we should use Lambda layer with
    // GetLatestConfiguration method instead
    public boolean shouldUseCache() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder().GET().uri(createAppConfigUri()).build();
        return attempt(() -> client.send(request, BodyHandlers.ofString()))
                   .map(HttpResponse::body)
                   .map(JsonUtils.dtoObjectMapper::readTree)
                    .map(jsonNode -> jsonNode.get(PUBLICATION_CHANNEL_CACHE_ENABLED_CONFIG_PARAM))
                    .map(JsonNode::asBoolean)
                    .orElse(failure -> false);
    }

    private static URI createAppConfigUri() {
        return UriWrapper.fromHost("http://localhost", 2772)
            .addChild("applications")
            .addChild(ENVIRONMENT.readEnv("APPLICATION_CONFIG_NAME"))
            .addChild("environments")
            .addChild(ENVIRONMENT.readEnv("APPLICATION_CONFIG_ENVIRONMENT_NAME"))
            .addChild("configurations")
            .addChild(ENVIRONMENT.readEnv("APPLICATION_CONFIG_PROFILE_NAME"))
            .getUri();
    }
}
