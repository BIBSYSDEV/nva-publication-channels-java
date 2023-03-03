package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.dataporten.model.DataportenCreateJournalRequest;
import no.sikt.nva.pubchannels.dataporten.model.DataportenCreateJournalResponse;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublicationChannel;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelClient {

    ThirdPartyPublicationChannel getJournal(String identifier, String year) throws ApiGatewayException;

    ThirdPartyPublicationChannel getPublisher(String identifier, String year) throws ApiGatewayException;

    DataportenCreateJournalResponse createJournal(DataportenCreateJournalRequest request) throws ApiGatewayException;
}
