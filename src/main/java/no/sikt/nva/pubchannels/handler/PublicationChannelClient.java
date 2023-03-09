package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreatePublisherRequest;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreatePublisherResponse;
import no.sikt.nva.pubchannels.dataporten.model.DataportenCreateJournalRequest;
import no.sikt.nva.pubchannels.dataporten.model.DataportenCreateJournalResponse;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublicationChannel;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelClient {

    ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
            throws ApiGatewayException;

    DataportenCreateJournalResponse createJournal(DataportenCreateJournalRequest request) throws ApiGatewayException;

    DataportenCreatePublisherResponse createPublisher(DataportenCreatePublisherRequest request)
            throws ApiGatewayException;
}
