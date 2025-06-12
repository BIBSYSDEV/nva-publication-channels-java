package no.sikt.nva.pubchannels.handler.fetch;

import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.function.Function;
import no.sikt.nva.pubchannels.channelregistry.PublicationChannelMovedException;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySerialPublication;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.model.PublicationChannelDto;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.attempt.Failure;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublicationChannelService {

  private static final String FETCHING_FROM_CHANNEL_REGISTER_MESSAGE =
      "Fetching {} from channel register: {}";
  private static final String FETCHING_FROM_CACHE_MESSAGE = "Fetching {} from cache: {}";
  private static final String ENV_API_DOMAIN = "API_DOMAIN";
  private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
  private static final Logger LOGGER = LoggerFactory.getLogger(PublicationChannelService.class);
  private final PublicationChannelFetchClient publicationChannelClient;
  private final CacheService cacheService;
  private final AppConfig appConfig;
  private final Environment environment;

  public PublicationChannelService(
      PublicationChannelFetchClient publicationChannelClient,
      CacheService cacheService,
      AppConfig appConfig,
      Environment environment) {
    this.publicationChannelClient = publicationChannelClient;
    this.cacheService = cacheService;
    this.appConfig = appConfig;
    this.environment = environment;
  }

  public URI constructPublicationChannelIdBaseUri(String type) {
    var apiDomain = environment.readEnv(ENV_API_DOMAIN);
    var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
    return new UriWrapper(HTTPS, apiDomain).addChild(customDomainBasePath, type).getUri();
  }

  public boolean shouldUseCache() {
    return appConfig.shouldUseCache();
  }

  public ThirdPartyPublicationChannel fetchChannelFromCache(RequestObject requestObject)
      throws ApiGatewayException {
    LOGGER.info(
        FETCHING_FROM_CACHE_MESSAGE, requestObject.channelType(), requestObject.identifier());
    return cacheService.getChannel(requestObject);
  }

  public PublicationChannelDto fetch(RequestObject requestObject) throws ApiGatewayException {
    var basUri =
        constructPublicationChannelIdBaseUri(requestObject.channelType().getNvaPathElement());
    var year = requestObject.getYear().orElse(null);

    var channel = fetchChannel(requestObject);

    return switch (channel) {
      case ChannelRegistrySerialPublication serialPublication ->
          SerialPublicationDto.create(basUri, serialPublication, year);
      case ThirdPartyPublisher publisher -> PublisherDto.create(basUri, publisher, year);
      default -> throw new IllegalStateException("Unexpected value: " + channel);
    };
  }

  public ThirdPartyPublicationChannel fetchChannelOrFetchFromCache(RequestObject requestObject)
      throws ApiGatewayException {
    try {
      return fetchChannelFromChannelRegister(requestObject);
    } catch (ApiGatewayException e) {
      return fetchFromCacheWhenServerError(requestObject, e);
    }
  }

  private static boolean isServerError(ApiGatewayException e) {
    return e.getStatusCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR;
  }

  private static Function<Failure<ThirdPartyPublicationChannel>, ApiGatewayException>
      throwOriginalException(ApiGatewayException e) {
    return failure -> e;
  }

  private ThirdPartyPublicationChannel fetchChannel(RequestObject requestObject)
      throws ApiGatewayException {
    return shouldUseCache()
        ? fetchChannelFromCache(requestObject)
        : fetchChannelOrFetchFromCache(requestObject);
  }

  private ThirdPartyPublicationChannel fetchFromCacheWhenServerError(
      RequestObject requestObject, ApiGatewayException e) throws ApiGatewayException {
    if (isServerError(e)) {
      return attempt(() -> fetchChannelFromCache(requestObject))
          .orElseThrow(throwOriginalException(e));
    } else {
      throw e;
    }
  }

  private ThirdPartyPublicationChannel fetchChannelFromChannelRegister(RequestObject requestObject)
      throws ApiGatewayException {
    try {
      LOGGER.info(
          FETCHING_FROM_CHANNEL_REGISTER_MESSAGE,
          requestObject.channelType(),
          requestObject.identifier());
      return publicationChannelClient.getChannel(requestObject);
    } catch (PublicationChannelMovedException movedException) {
      throw new PublicationChannelMovedException(
          "%s moved".formatted(requestObject.channelType()),
          constructNewLocation(movedException.getLocation(), requestObject));
    }
  }

  private URI constructNewLocation(URI channelRegistryLocation, RequestObject requestObject) {
    var newIdentifier =
        UriWrapper.fromUri(channelRegistryLocation).getPath().getPathElementByIndexFromEnd(1);
    var uriWrapper =
        UriWrapper.fromUri(
                constructPublicationChannelIdBaseUri(
                    requestObject.channelType().getNvaPathElement()))
            .addChild(newIdentifier);
    if (requestObject.getYear().isPresent()) {
      return uriWrapper.addChild(requestObject.getYear().get()).getUri();
    }
    return uriWrapper.getUri();
  }
}
