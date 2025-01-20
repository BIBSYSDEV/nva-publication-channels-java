package no.sikt.nva.pubchannels.handler.create.journal;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.JOURNAL;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreateJournalHandler
    extends CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> {

  private static final String JOURNAL_PATH_ELEMENT = "journal";

  @JacocoGenerated
  public CreateJournalHandler() {
    super(CreateSerialPublicationRequest.class, new Environment());
  }

  public CreateJournalHandler(
      Environment environment, PublicationChannelClient publicationChannelClient) {
    super(CreateSerialPublicationRequest.class, environment, publicationChannelClient);
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
        publicationChannelClient.createJournal(
            ChannelRegistryCreateSerialPublicationRequest.fromClientRequest(request));

    // Fetch the new journal from the channel registry to build the full response
    var year = getYear();
    var newJournal =
        (ThirdPartySerialPublication)
            publicationChannelClient.getChannel(JOURNAL, response.pid(), year);
    var journalDto =
        SerialPublicationDto.create(constructBaseUri(JOURNAL_PATH_ELEMENT), newJournal, year);

    addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, journalDto.id().toString()));
    return journalDto;
  }
}
