package no.sikt.nva.pubchannels.handler;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface AuthClient {

  String getToken() throws ApiGatewayException;
}
