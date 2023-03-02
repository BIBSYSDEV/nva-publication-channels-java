package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.dataporten.model.CreateJournalResponse;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyJournal;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelClient {

    ThirdPartyJournal getJournal(String identifier, String year) throws ApiGatewayException;

    CreateJournalResponse createJournal(String name) throws ApiGatewayException;
}
