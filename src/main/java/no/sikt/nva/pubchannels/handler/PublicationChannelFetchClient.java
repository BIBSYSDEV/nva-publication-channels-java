package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.handler.fetch.RequestObject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelFetchClient {

  ThirdPartyPublicationChannel getChannel(RequestObject requestObject) throws ApiGatewayException;
}
