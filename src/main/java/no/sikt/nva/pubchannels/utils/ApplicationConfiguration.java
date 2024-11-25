package no.sikt.nva.pubchannels.utils;

import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.JsonNode;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.services.appconfig.AppConfigClient;
import software.amazon.awssdk.services.appconfig.model.GetConfigurationRequest;
import software.amazon.awssdk.services.appconfig.model.GetConfigurationResponse;

public class ApplicationConfiguration implements AppConfig {

    public static final String PUBLICATION_CHANNEL_CACHE_ENABLED_CONFIG_PARAM = "publicationChannelCacheEnabled";
    private static final Environment ENVIRONMENT = new Environment();
    private final AppConfigClient client;

    public ApplicationConfiguration(AppConfigClient client) {
        this.client = client;
    }

    @JacocoGenerated
    public static ApplicationConfiguration defaultAppConfigClientInstance() {
        return new ApplicationConfiguration(AppConfigClient.create());
    }

    @Override
    public boolean shouldUseCache() {
        return attempt(() -> client.getConfiguration(createRequest()))
                   .map(GetConfigurationResponse::content)
                   .map(BytesWrapper::asUtf8String)
                   .map(JsonUtils.dtoObjectMapper::readTree)
                   .map(jsonNode -> jsonNode.get(PUBLICATION_CHANNEL_CACHE_ENABLED_CONFIG_PARAM))
                   .map(JsonNode::asBoolean)
                   .orElse(failure -> false);
    }

    private static GetConfigurationRequest createRequest() {
        return GetConfigurationRequest.builder()
                   .application(ENVIRONMENT.readEnv("APPLICATION_CONFIG_NAME"))
                   .environment(ENVIRONMENT.readEnv("APPLICATION_CONFIG_ENVIRONMENT_NAME"))
                   .configuration(ENVIRONMENT.readEnv("APPLICATION_CONFIG_PROFILE_NAME"))
                   .clientId(ENVIRONMENT.readEnv("APPLICATION_ID"))
                   .build();
    }
}
