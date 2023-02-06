package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.model.Journal;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelSource {

    Journal getJournal(String identifier, String year) throws ApiGatewayException;
}
