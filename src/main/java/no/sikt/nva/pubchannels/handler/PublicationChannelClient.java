package no.sikt.nva.pubchannels.handler;

import java.util.Map;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreateJournalRequest;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreateJournalResponse;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreatePublisherRequest;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreatePublisherResponse;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreateSeriesRequest;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreateSeriesResponse;
import no.sikt.nva.pubchannels.dataporten.search.DataportenSearchResponse;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublisher;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelClient {

    ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
        throws ApiGatewayException;

    DataportenSearchResponse getChannel(ChannelType type, Map<String, String> queryParameters)
        throws ApiGatewayException;

    ThirdPartyPublisher getPublisher(String identifier, String year) throws ApiGatewayException;

    DataportenCreateJournalResponse createJournal(DataportenCreateJournalRequest request) throws ApiGatewayException;

    DataportenCreatePublisherResponse createPublisher(DataportenCreatePublisherRequest request)
        throws ApiGatewayException;

    DataportenCreateSeriesResponse createSeries(DataportenCreateSeriesRequest request) throws ApiGatewayException;
}
