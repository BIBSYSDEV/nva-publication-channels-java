package no.sikt.nva.pubchannels.handler;

import java.util.Map;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreatePublisherRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.handler.search.ThirdPartySearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelSearchCreateClient {

  ThirdPartySearchResponse searchChannel(ChannelType type, Map<String, String> queryParameters)
      throws ApiGatewayException;

  CreateChannelResponse createJournal(ChannelRegistryCreateSerialPublicationRequest request)
      throws ApiGatewayException;

  CreateChannelResponse createPublisher(ChannelRegistryCreatePublisherRequest request)
      throws ApiGatewayException;

  CreateChannelResponse createSeries(ChannelRegistryCreateSerialPublicationRequest request)
      throws ApiGatewayException;
}
