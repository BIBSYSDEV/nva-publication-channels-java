package no.sikt.nva.pubchannels.handler.fetch;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.net.HttpURLConnection.HTTP_OK;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.model.PublicationChannelDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import no.sikt.nva.pubchannels.utils.ApplicationConfiguration;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchPublicationChannelHandler extends ApiGatewayHandler<Void, PublicationChannelDto> {

  private final PublicationChannelService publicationChannelService;

  @JacocoGenerated
  public FetchPublicationChannelHandler() {
    super(Void.class, new Environment());
    this.publicationChannelService =
        new PublicationChannelService(
            ChannelRegistryClient.defaultInstance(),
            CacheService.defaultInstance(),
            ApplicationConfiguration.defaultAppConfigClientInstance(),
            new Environment());
  }

  public FetchPublicationChannelHandler(
      Environment environment,
      PublicationChannelFetchClient channelRegistryClient,
      CacheService cacheService,
      AppConfig appConfigWithCacheEnabled) {
    super(Void.class, environment);
    this.publicationChannelService =
        new PublicationChannelService(
            channelRegistryClient, cacheService, appConfigWithCacheEnabled, environment);
  }

  @Override
  protected List<MediaType> listSupportedMediaTypes() {
    return List.of(JSON_UTF_8, APPLICATION_JSON_LD);
  }

  @Override
  protected void validateRequest(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {}

  @Override
  protected PublicationChannelDto processInput(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    return publicationChannelService.fetch(RequestObject.fromRequestInfo(requestInfo));
  }

  @Override
  protected Integer getSuccessStatusCode(Void input, PublicationChannelDto output) {
    return HTTP_OK;
  }
}
