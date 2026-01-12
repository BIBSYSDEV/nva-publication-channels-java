package no.sikt.nva.pubchannels.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nva.commons.core.Environment;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.FlushStageCacheRequest;
import software.amazon.awssdk.services.apigateway.model.FlushStageCacheResponse;

class CacheInvalidatorTest {

  private static final Environment ENVIRONMENT = new Environment();
  private static final String REST_API_ID = ENVIRONMENT.readEnv("REST_API_ID");
  private static final String STAGE_NAME = ENVIRONMENT.readEnv("API_STAGE_NAME");

  @Test
  void shouldCallApiGatewayClient() {
    var apiGatewayClient = mock(ApiGatewayClient.class);
    when(apiGatewayClient.flushStageCache(any(FlushStageCacheRequest.class)))
        .thenReturn(FlushStageCacheResponse.builder().build());

    var cacheInvalidator = new CacheInvalidator(apiGatewayClient, ENVIRONMENT);
    cacheInvalidator.invalidateCache();

    verify(apiGatewayClient)
        .flushStageCache(
            FlushStageCacheRequest.builder().restApiId(REST_API_ID).stageName(STAGE_NAME).build());
  }

  @Test
  void shouldNotThrowWhenFlushStageCacheFails() {
    var apiGatewayClient = mock(ApiGatewayClient.class);
    when(apiGatewayClient.flushStageCache(any(FlushStageCacheRequest.class)))
        .thenThrow(new RuntimeException("Test exception"));

    var cacheInvalidator = new CacheInvalidator(apiGatewayClient, ENVIRONMENT);
    cacheInvalidator.invalidateCache();
  }
}
