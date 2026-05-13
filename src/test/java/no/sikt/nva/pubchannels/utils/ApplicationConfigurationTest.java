package no.sikt.nva.pubchannels.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.appconfig.model.ResourceNotFoundException;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse;

class ApplicationConfigurationTest {

  @Test
  void shouldReturnTrueWhenAppConfigHasCachingEnabled() {
    var client = mock(AppConfigDataClient.class);
    when(client.getLatestConfiguration(any(GetLatestConfigurationRequest.class)))
        .thenReturn(mockedLatestResponse());
    when(client.startConfigurationSession(any(StartConfigurationSessionRequest.class)))
        .thenReturn(mockedStartResponse());
    var appConfig = new ApplicationConfiguration(client);

    assertTrue(appConfig.shouldUseCache());
  }

  @Test
  void shouldReturnFalseWhenFetchingConfigurationFails() {
    var client = mock(AppConfigDataClient.class);
    when(client.getLatestConfiguration(any(GetLatestConfigurationRequest.class)))
        .thenThrow(ResourceNotFoundException.class);
    when(client.startConfigurationSession(any(StartConfigurationSessionRequest.class)))
        .thenReturn(mockedStartResponse());
    var appConfig = new ApplicationConfiguration(client);

    assertFalse(appConfig.shouldUseCache());
  }

  private static GetLatestConfigurationResponse mockedLatestResponse() {
    return GetLatestConfigurationResponse.builder()
        .configuration(SdkBytes.fromString(configContent(), StandardCharsets.UTF_8))
        .build();
  }

  private static String configContent() {
    return """
    {
        "publicationChannelCacheEnabled": true
    }
    """;
  }

  private static StartConfigurationSessionResponse mockedStartResponse() {
    return StartConfigurationSessionResponse.builder()
        .initialConfigurationToken("mockToken")
        .build();
  }
}
