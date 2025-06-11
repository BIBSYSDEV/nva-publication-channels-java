package no.sikt.nva.pubchannels.handler.fetch.serialpublication;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchSerialPublicationByIdentifierAndYearHandler
    extends FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> {

  private static final String SERIAL_PUBLICATION_PATH_ELEMENT = "serial-publication";

  @JacocoGenerated
  public FetchSerialPublicationByIdentifierAndYearHandler() {
    super(Void.class, new Environment());
  }

  public FetchSerialPublicationByIdentifierAndYearHandler(
      Environment environment,
      PublicationChannelClient channelRegistryClient,
      CacheService cacheService,
      AppConfig appConfig) {
    super(Void.class, environment, channelRegistryClient, cacheService, appConfig);
  }

  @Override
  protected SerialPublicationDto processInput(Void unused, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    var requestObject = RequestObject.from(requestInfo);
    var serialPublicationBaseUri =
        constructPublicationChannelIdBaseUri(SERIAL_PUBLICATION_PATH_ELEMENT);
    var year = requestObject.year().orElse(null);
    var serialPublication =
        super.shouldUseCache()
            ? super.fetchChannelFromCache(requestObject)
            : super.fetchChannelOrFetchFromCache(requestObject);
    return SerialPublicationDto.create(
        serialPublicationBaseUri, (ThirdPartySerialPublication) serialPublication, year);
  }

  @Override
  protected String getPathElement() {
    return SERIAL_PUBLICATION_PATH_ELEMENT;
  }
}
