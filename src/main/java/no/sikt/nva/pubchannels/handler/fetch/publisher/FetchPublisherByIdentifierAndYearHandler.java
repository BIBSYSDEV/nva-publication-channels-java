package no.sikt.nva.pubchannels.handler.fetch.publisher;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.fetch.serialpublication.RequestObject;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchPublisherByIdentifierAndYearHandler
    extends FetchByIdentifierAndYearHandler<Void, PublisherDto> {

  private static final String PUBLISHER_PATH_ELEMENT = "publisher";

  @JacocoGenerated
  public FetchPublisherByIdentifierAndYearHandler() {
    super(Void.class, new Environment());
  }

  public FetchPublisherByIdentifierAndYearHandler(
      Environment environment,
      PublicationChannelClient publicationChannelClient,
      CacheService cacheService,
      AppConfig appConfig) {
    super(Void.class, environment, publicationChannelClient, cacheService, appConfig);
  }

  @Override
  protected PublisherDto processInput(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    var requestObject = RequestObject.from(requestInfo);
    var publisherIdBaseUri = constructPublicationChannelIdBaseUri(PUBLISHER_PATH_ELEMENT);
    var year = requestObject.year();

    var publisher =
        super.shouldUseCache()
            ? super.fetchChannelFromCache(requestObject)
            : super.fetchChannelOrFetchFromCache(requestObject);
    return PublisherDto.create(publisherIdBaseUri, (ThirdPartyPublisher) publisher, year.get());
  }

  @Override
  protected String getPathElement() {
    return PUBLISHER_PATH_ELEMENT;
  }
}
