package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelFetchClient {

  ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
      throws ApiGatewayException;
}
