package no.sikt.nva.pubchannels.utils;

import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.databind.JsonNode;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;
import java.util.concurrent.atomic.AtomicReference;

public class ApplicationConfiguration implements AppConfig {

    public static final String PUBLICATION_CHANNEL_CACHE_ENABLED_CONFIG_PARAM = "publicationChannelCacheEnabled";
    private static final Environment ENVIRONMENT = new Environment();
    private final AppConfigDataClient client;
    private static final long CACHE_DURATION_MILLIS = 60_000; // 1 minute

    private final AtomicReference<CacheEntry> cache = new AtomicReference<>();
    private final String configurationToken;

    public ApplicationConfiguration(AppConfigDataClient client) {
        this.client = client;
        this.configurationToken = startConfigurationSession();
    }

    @JacocoGenerated
    public static ApplicationConfiguration defaultAppConfigClientInstance() {
        return new ApplicationConfiguration(AppConfigDataClient.create());
    }

    @Override
    public boolean shouldUseCache() {
        var entry = cache.get();
        long currentTime = System.currentTimeMillis();
        if (entry == null || currentTime - entry.timestamp > CACHE_DURATION_MILLIS) {
            boolean shouldUseCache = fetchShouldUseCache();
            cache.set(new CacheEntry(shouldUseCache, currentTime));
            return shouldUseCache;
        }
        return entry.shouldUseCache;
    }

    private boolean fetchShouldUseCache() {
        return attempt(() -> client.getLatestConfiguration(createGetLatestConfigurationRequest()))
                   .map(GetLatestConfigurationResponse::configuration)
                   .map(BytesWrapper::asUtf8String)
                   .map(JsonUtils.dtoObjectMapper::readTree)
                   .map(jsonNode -> jsonNode.get(PUBLICATION_CHANNEL_CACHE_ENABLED_CONFIG_PARAM))
                   .map(JsonNode::asBoolean)
                   .orElse(failure -> false);
    }

    private String startConfigurationSession() {
        var request = StartConfigurationSessionRequest.builder()
                                                       .applicationIdentifier(ENVIRONMENT.readEnv("APPLICATION_CONFIG_NAME"))
                                                       .environmentIdentifier(ENVIRONMENT.readEnv("APPLICATION_CONFIG_ENVIRONMENT_NAME"))
                                                       .configurationProfileIdentifier(ENVIRONMENT.readEnv("APPLICATION_CONFIG_PROFILE_NAME"))
                                                       .build();
        var response = client.startConfigurationSession(request);
        return response.initialConfigurationToken();
    }

    private GetLatestConfigurationRequest createGetLatestConfigurationRequest() {
        return GetLatestConfigurationRequest.builder()
                   .configurationToken(configurationToken)
                   .build();
    }

    private record CacheEntry(boolean shouldUseCache, long timestamp) {
    }
}