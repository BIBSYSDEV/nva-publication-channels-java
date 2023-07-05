package no.sikt.nva.pubchannels.handler;

import java.util.Map;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreateJournalRequest;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreateJournalResponse;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreatePublisherRequest;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreatePublisherResponse;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreateSeriesRequest;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreateSeriesResponse;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelClient {

    ThirdPartyPublicationChannel getChannel(ChannelType type, String identifier, String year)
        throws ApiGatewayException;

    ThirdPartySearchResponse searchChannel(ChannelType type, Map<String, String> queryParameters)
        throws ApiGatewayException;

    DataportenCreateJournalResponse createJournal(DataportenCreateJournalRequest request) throws ApiGatewayException;

    DataportenCreatePublisherResponse createPublisher(DataportenCreatePublisherRequest request)
        throws ApiGatewayException;

    DataportenCreateSeriesResponse createSeries(DataportenCreateSeriesRequest request) throws ApiGatewayException;
}
