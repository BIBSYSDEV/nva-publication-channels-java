package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.dataporten.create.DataportenCreatePublisherRequest;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreatePublisherResponse;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreateSeriesRequest;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreateSeriesResponse;
import no.sikt.nva.pubchannels.dataporten.model.DataportenCreateJournalRequest;
import no.sikt.nva.pubchannels.dataporten.model.DataportenCreateJournalResponse;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyJournal;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelClient {

    ThirdPartyJournal getJournal(String identifier, String year) throws ApiGatewayException;

    DataportenCreateJournalResponse createJournal(DataportenCreateJournalRequest request) throws ApiGatewayException;

    DataportenCreatePublisherResponse createPublisher(DataportenCreatePublisherRequest request)
            throws ApiGatewayException;

    DataportenCreateSeriesResponse createSeries(DataportenCreateSeriesRequest request) throws ApiGatewayException;
}
