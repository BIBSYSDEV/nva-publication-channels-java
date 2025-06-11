package no.sikt.nva.pubchannels.handler.fetch;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.net.HttpURLConnection.HTTP_OK;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.fetch.serialpublication.RequestObject;
import no.sikt.nva.pubchannels.handler.model.PublicationChannelDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

public class FetchPublicationChannelHandler extends ApiGatewayHandler<Void, PublicationChannelDto> {

  private final Fetcher fetcher;

  public FetchPublicationChannelHandler(
      Environment environment,
      PublicationChannelFetchClient channelRegistryClient,
      CacheService cacheService,
      AppConfig appConfigWithCacheEnabled) {
    super(Void.class, environment);
    this.fetcher =
        new Fetcher(channelRegistryClient, cacheService, appConfigWithCacheEnabled, environment);
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
    return fetcher.fetch(RequestObject.from(requestInfo));
  }

  @Override
  protected Integer getSuccessStatusCode(Void input, PublicationChannelDto output) {
    return HTTP_OK;
  }
}
