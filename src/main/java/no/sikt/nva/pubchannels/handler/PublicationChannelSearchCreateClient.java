package no.sikt.nva.pubchannels.handler;

import java.util.Map;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateJournalRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreatePublisherRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSeriesRequest;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelSearchCreateClient {

    ThirdPartySearchResponse searchChannel(ChannelType type, Map<String, String> queryParameters)
        throws ApiGatewayException;

    CreateChannelResponse createJournal(ChannelRegistryCreateJournalRequest request) throws ApiGatewayException;

    CreateChannelResponse createPublisher(ChannelRegistryCreatePublisherRequest request)
        throws ApiGatewayException;

    CreateChannelResponse createSeries(ChannelRegistryCreateSeriesRequest request) throws ApiGatewayException;
}
