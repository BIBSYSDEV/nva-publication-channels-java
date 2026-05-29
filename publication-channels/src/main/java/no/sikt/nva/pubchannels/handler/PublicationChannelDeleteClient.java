package no.sikt.nva.pubchannels.handler;

import nva.commons.apigateway.exceptions.ApiGatewayException;

@FunctionalInterface
public interface PublicationChannelDeleteClient {

  void deleteChannel(String identifier) throws ApiGatewayException;
}
