package no.sikt.nva.pubchannels.handler.fetch.series;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.fetch.serialpublication.RequestObject;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchSeriesByIdentifierAndYearHandler
    extends FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> {

  private static final String SERIES_PATH_ELEMENT = "series";

  @JacocoGenerated
  public FetchSeriesByIdentifierAndYearHandler() {
    super(Void.class, new Environment());
  }

  public FetchSeriesByIdentifierAndYearHandler(
      Environment environment,
      PublicationChannelClient publicationChannelClient,
      CacheService cacheService,
      AppConfig appConfig) {
    super(Void.class, environment, publicationChannelClient, cacheService, appConfig);
  }

  @Override
  protected SerialPublicationDto processInput(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    var requestObject = RequestObject.from(requestInfo);
    var publisherIdBaseUri = constructPublicationChannelIdBaseUri(SERIES_PATH_ELEMENT);
    var year = requestObject.year();

    var series =
        super.shouldUseCache()
            ? super.fetchChannelFromCache(requestObject)
            : super.fetchChannelOrFetchFromCache(requestObject);
    return SerialPublicationDto.create(
        publisherIdBaseUri, (ThirdPartySerialPublication) series, year.get());
  }

  @Override
  protected String getPathElement() {
    return SERIES_PATH_ELEMENT;
  }
}
