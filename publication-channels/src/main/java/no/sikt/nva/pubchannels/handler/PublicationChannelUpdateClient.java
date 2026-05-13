package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryUpdateChannelRequest;
import nva.commons.apigateway.exceptions.ApiGatewayException;

@FunctionalInterface
public interface PublicationChannelUpdateClient {

  void updateChannel(ChannelRegistryUpdateChannelRequest request) throws ApiGatewayException;
}
