package no.sikt.nva.pubchannels.handler;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelSource {

    ThirdPartyJournal getJournal(String identifier, String year) throws ApiGatewayException;
}
