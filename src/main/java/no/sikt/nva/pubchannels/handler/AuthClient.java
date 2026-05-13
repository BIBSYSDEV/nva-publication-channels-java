package no.sikt.nva.pubchannels.handler;

import nva.commons.apigateway.exceptions.ApiGatewayException;

@FunctionalInterface
public interface AuthClient {

  String getToken() throws ApiGatewayException;
}
