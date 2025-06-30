package no.sikt.nva.pubchannels.handler.update;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import static nva.commons.core.attempt.Try.attempt;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.UpdateChannelRequest;
import no.sikt.nva.pubchannels.handler.PublicationChannelUpdateClient;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class UpdatePublicationChannelHandler extends ApiGatewayHandler<UpdateChannelRequest, Void> {

  protected static final String IDENTIFIER = "identifier";
  protected static final String IDENTIFIER_PARAMETER_IS_MISSING = "Identifier parameter is missing";
  protected static final String INVALID_IDENTIFIER_MESSAGE =
      "Provided identifier is not valid UUID";
  protected static final String EMPTY_REQUEST_MESSAGE = "Request body is empty!";
  private final PublicationChannelUpdateClient client;

  @JacocoGenerated
  public UpdatePublicationChannelHandler() {
    this(ChannelRegistryClient.defaultAuthorizedInstance(new Environment()));
  }

  public UpdatePublicationChannelHandler(PublicationChannelUpdateClient client) {
    super(UpdateChannelRequest.class, new Environment());
    this.client = client;
  }

  @Override
  protected void validateRequest(
      UpdateChannelRequest input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    if (!requestInfo.isGatewayAuthorized()) {
      throw new UnauthorizedException();
    }
    if (!requestInfo.userIsAuthorized(AccessRight.MANAGE_CUSTOMERS)) {
      throw new ForbiddenException();
    }
    if (!requestInfo.getPathParameters().containsKey(IDENTIFIER)) {
      throw new BadRequestException(IDENTIFIER_PARAMETER_IS_MISSING);
    }

    attempt(() -> UUID.fromString(requestInfo.getPathParameter(IDENTIFIER)))
        .orElseThrow(failure -> new BadRequestException(INVALID_IDENTIFIER_MESSAGE));

    if (input.isEmpty()) {
      throw new BadRequestException(EMPTY_REQUEST_MESSAGE);
    }
  }

  @Override
  protected Void processInput(UpdateChannelRequest input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    var identifier = requestInfo.getPathParameter(IDENTIFIER);
    client.updateChannel(input.toChannelRegistryUpdateRequest(identifier));
    return null;
  }

  @Override
  protected Integer getSuccessStatusCode(UpdateChannelRequest input, Void output) {
    return HTTP_ACCEPTED;
  }
}
