package no.sikt.nva.pubchannels.handler.delete;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static nva.commons.core.attempt.Try.attempt;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelDeleteClient;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class DeletePublicationChannelHandler extends ApiGatewayHandler<Void, Void> {

  protected static final String IDENTIFIER = "identifier";
  protected static final String IDENTIFIER_PARAMETER_IS_MISSING = "Identifier parameter is missing";
  protected static final String INVALID_IDENTIFIER_MESSAGE =
      "Provided identifier is not valid UUID";
  private final PublicationChannelDeleteClient client;

  @JacocoGenerated
  public DeletePublicationChannelHandler() {
    this(ChannelRegistryClient.defaultAuthorizedInstance(new Environment()));
  }

  public DeletePublicationChannelHandler(PublicationChannelDeleteClient client) {
    super(Void.class, new Environment());
    this.client = client;
  }

  @Override
  protected void validateRequest(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    if (!requestInfo.isGatewayAuthorized()) {
      throw new UnauthorizedException();
    }
    if (!requestInfo.clientIsInternalBackend()) {
      throw new ForbiddenException();
    }
    if (!requestInfo.getPathParameters().containsKey(IDENTIFIER)) {
      throw new BadRequestException(IDENTIFIER_PARAMETER_IS_MISSING);
    }
    attempt(() -> UUID.fromString(requestInfo.getPathParameter(IDENTIFIER)))
        .orElseThrow(failure -> new BadRequestException(INVALID_IDENTIFIER_MESSAGE));
  }

  @Override
  protected Void processInput(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    client.deleteChannel(requestInfo.getPathParameter(IDENTIFIER));
    return null;
  }

  @Override
  protected Integer getSuccessStatusCode(Void input, Void output) {
    return HTTP_NO_CONTENT;
  }
}
