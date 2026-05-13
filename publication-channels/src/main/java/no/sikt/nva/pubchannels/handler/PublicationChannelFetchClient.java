package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.handler.fetch.RequestObject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

@FunctionalInterface
public interface PublicationChannelFetchClient {

  ThirdPartyPublicationChannel getChannel(RequestObject requestObject) throws ApiGatewayException;
}
