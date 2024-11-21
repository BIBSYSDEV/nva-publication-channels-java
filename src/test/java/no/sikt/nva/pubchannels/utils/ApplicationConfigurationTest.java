package no.sikt.nva.pubchannels.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.appconfig.AppConfigClient;
import software.amazon.awssdk.services.appconfig.model.GetConfigurationRequest;
import software.amazon.awssdk.services.appconfig.model.GetConfigurationResponse;

class ApplicationConfigurationTest {

    @Test
    void shouldReturnTrueWhenAppConfigHasCachingEnabled() throws IOException {
        var client = mock(AppConfigClient.class);
        when(client.getConfiguration(any(GetConfigurationRequest.class))).thenReturn(mockedResponse());
        var appConfig = new ApplicationConfiguration(client);

        assertTrue(appConfig.shouldUseCache());
    }

    @Test
    void some() throws IOException {
        var s = new ApplicationConfiguration(AppConfigClient.create()).shouldUseCache();
    }

    private static GetConfigurationResponse mockedResponse() {
        return GetConfigurationResponse.builder()
                   .content(SdkBytes.fromString(configContent(), StandardCharsets.UTF_8))
                   .build();
    }

    private static String configContent() {
        return """
            {
                "publicationChannelCacheEnabled": true
            }
            """;
    }
}