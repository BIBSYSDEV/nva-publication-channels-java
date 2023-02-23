package no.sikt.nva.pubchannels.handler;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelClient {

    ThirdPartyJournal getJournal(String identifier, String year) throws ApiGatewayException;

    String createJournal(String name) throws ApiGatewayException;
}
