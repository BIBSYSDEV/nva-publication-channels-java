package no.sikt.nva.pubchannels.utils;

import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.FlushStageCacheRequest;

public class CacheInvalidator {

  private static final Logger logger = LoggerFactory.getLogger(CacheInvalidator.class);
  private static final String REST_API_ID_ENV = "REST_API_ID";
  private static final String STAGE_NAME_ENV = "API_STAGE_NAME";

  private final ApiGatewayClient apiGatewayClient;
  private final String restApiId;
  private final String stageName;

  @JacocoGenerated
  public CacheInvalidator() {
    this(ApiGatewayClient.create(), new Environment());
  }

  public CacheInvalidator(ApiGatewayClient apiGatewayClient, Environment environment) {
    this.apiGatewayClient = apiGatewayClient;
    this.restApiId = environment.readEnv(REST_API_ID_ENV);
    this.stageName = environment.readEnv(STAGE_NAME_ENV);
  }

  public void invalidateCache() {
    try {
      logger.info(
          "Invalidating API Gateway cache for REST API: {}, stage: {}", restApiId, stageName);
      apiGatewayClient.flushStageCache(
          FlushStageCacheRequest.builder().restApiId(restApiId).stageName(stageName).build());
      logger.info("Cache invalidation successful");
    } catch (Exception e) {
      logger.warn("Failed to invalidate cache: {}", e.getMessage());
    }
  }
}
