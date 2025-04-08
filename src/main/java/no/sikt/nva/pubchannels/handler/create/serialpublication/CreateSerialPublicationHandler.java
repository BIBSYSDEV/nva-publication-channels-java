package no.sikt.nva.pubchannels.handler.create.serialpublication;

import static java.util.Objects.isNull;
import static no.sikt.nva.pubchannels.channelregistry.ChannelType.SERIAL_PUBLICATION;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreateSerialPublicationHandler
    extends CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> {

  private static final String JOURNAL = "journal";
  private static final String SERIES = "series";
  private static final String CUSTOM_PATH_ELEMENT = "serial-publication";

  @JacocoGenerated
  public CreateSerialPublicationHandler() {
    super(CreateSerialPublicationRequest.class, new Environment());
  }

  public CreateSerialPublicationHandler(
      Environment environment, ChannelRegistryClient channelRegistryClient) {
    super(CreateSerialPublicationRequest.class, environment, channelRegistryClient);
  }

  @Override
  protected void validateRequest(
      CreateSerialPublicationRequest request, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    userIsAuthorizedToCreate(requestInfo);
    request.validate();
    validateType(request);
  }

  @Override
  protected SerialPublicationDto processInput(
      CreateSerialPublicationRequest request, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    var channelRegistryCreateRequest =
        ChannelRegistryCreateSerialPublicationRequest.fromClientRequest(request);
    var response =
        JOURNAL.equalsIgnoreCase(request.type())
            ? publicationChannelClient.createJournal(channelRegistryCreateRequest)
            : publicationChannelClient.createSeries(channelRegistryCreateRequest);

    // Fetch the new channel to build the full response
    var year = getYear();
    var newSerialPublication =
        (ThirdPartySerialPublication)
            publicationChannelClient.getChannel(SERIAL_PUBLICATION, response.pid(), year);
    var responseBody =
        SerialPublicationDto.create(
            constructBaseUri(CUSTOM_PATH_ELEMENT), newSerialPublication, year);

    addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, responseBody.id().toString()));
    return responseBody;
  }

  private void validateType(CreateSerialPublicationRequest input) throws BadRequestException {
    if (isNull(input.type())) {
      throw new BadRequestException(
          "Type cannot be null! Type must be either 'Journal' or 'Series'");
    }
    if (!JOURNAL.equalsIgnoreCase(input.type()) && !SERIES.equalsIgnoreCase(input.type())) {
      throw new BadRequestException("Type must be either 'Journal' or 'Series'");
    }
  }
}
