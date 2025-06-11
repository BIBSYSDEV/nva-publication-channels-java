package no.sikt.nva.pubchannels.handler.fetch.publisher;

import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySerialPublication;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.fetch.Fetcher;
import no.sikt.nva.pubchannels.handler.fetch.serialpublication.RequestObject;
import no.sikt.nva.pubchannels.handler.model.PublicationChannelDto;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class FetchPublisherByIdentifierAndYearHandler {

  private final RequestObject requestObject;
  private final Fetcher fetcher;

  public FetchPublisherByIdentifierAndYearHandler(RequestObject requestObject, Fetcher fetcher) {
    this.requestObject = requestObject;
    this.fetcher = fetcher;
  }

  public PublicationChannelDto processInput() throws ApiGatewayException {
    var basUri = fetcher.constructPublicationChannelIdBaseUri(requestObject.type().name());
    var year = requestObject.year().orElse(null);

    var channel = fetchChannel();

    return switch (channel) {
      case ChannelRegistrySerialPublication serialPublication ->
          SerialPublicationDto.create(basUri, serialPublication, year);
      case ThirdPartyPublisher publisher -> PublisherDto.create(basUri, publisher, year);
      default -> throw new IllegalStateException("Unexpected value: " + channel);
    };
  }

  private ThirdPartyPublicationChannel fetchChannel() throws ApiGatewayException {
    return fetcher.shouldUseCache()
        ? fetcher.fetchChannelFromCache(requestObject)
        : fetcher.fetchChannelOrFetchFromCache(requestObject);
  }
}
