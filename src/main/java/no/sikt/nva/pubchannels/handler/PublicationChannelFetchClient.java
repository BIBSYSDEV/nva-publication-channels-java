package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.fetch.serialpublication.RequestObject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelFetchClient {

  ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
      throws ApiGatewayException;

  ThirdPartyPublicationChannel getChannel(RequestObject requestObject) throws ApiGatewayException;
}
