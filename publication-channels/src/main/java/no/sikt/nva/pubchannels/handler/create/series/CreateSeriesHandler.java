package no.sikt.nva.pubchannels.handler.create.series;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.SERIES;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.fetch.RequestObject;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreateSeriesHandler
    extends CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> {

  private static final String SERIES_PATH_ELEMENT = "series";

  @JacocoGenerated
  public CreateSeriesHandler() {
    super(CreateSerialPublicationRequest.class, new Environment());
  }

  public CreateSeriesHandler(Environment environment, ChannelRegistryClient client) {
    super(CreateSerialPublicationRequest.class, environment, client);
  }

  @Override
  protected void validateRequest(
      CreateSerialPublicationRequest request, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    userIsAuthorizedToCreate(requestInfo);
    request.validate();
  }

  @Override
  protected SerialPublicationDto processInput(
      CreateSerialPublicationRequest request, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    var response =
        publicationChannelClient.createSeries(
            ChannelRegistryCreateSerialPublicationRequest.fromClientRequest(request));

    // Fetch the new series from the channel registry to build the full response
    var year = getYear();
    var requestObject = new RequestObject(SERIES, response.pid(), year);
    var newSeries =
        (ThirdPartySerialPublication) publicationChannelClient.getChannel(requestObject);
    var seriesDto =
        SerialPublicationDto.create(constructBaseUri(SERIES_PATH_ELEMENT), newSeries, year);

    addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, seriesDto.id().toString()));
    return seriesDto;
  }
}
