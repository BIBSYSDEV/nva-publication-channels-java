package no.sikt.nva.pubchannels.handler.update;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.UUID.randomUUID;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.AccessRight.MANAGE_CUSTOMERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import no.sikt.nva.pubchannels.channelregistry.UpdateChannelRequest;
import no.sikt.nva.pubchannels.channelregistry.UpdatePublisherRequest;
import no.sikt.nva.pubchannels.channelregistry.UpdateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelUpdateClient;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class UpdatePublicationChannelHandlerTest {

  protected static final FakeContext CONTEXT = new FakeContext();
  protected static final String IDENTIFIER = "identifier";
  private UpdatePublicationChannelHandler handler;
  private PublicationChannelUpdateClient client;
  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    this.output = new ByteArrayOutputStream();
    this.client = mock(PublicationChannelClient.class);
    this.handler = new UpdatePublicationChannelHandler(client);
  }

  @Test
  void shouldReturnUnauthorizedWhenUserIsNotAuthorized() throws IOException {
    var request = createUnauthorizedRequest();

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void shouldReturnForbiddenWhenUserHasNoManageCustomersAccessRight() throws IOException {
    var request = createRequestForUserWithoutAccessRights();

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_FORBIDDEN, response.getStatusCode());
  }

  @Test
  void shouldReturnBadRequestWhenIdentifierIsMissingInPathParam() throws IOException {
    var request = createRequestWithoutPathParams();

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldReturnBadRequestWhenIdentifierInPathParamIsNotValidUUID() throws IOException {
    var request = createRequest(randomString(), validRequestBody());

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldReturnBadRequestWhenRequestBodyIsMissingFieldsToUpdate() throws IOException {
    var request = createRequest(randomString(), new UpdatePublisherRequest(null, null));

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldReturnNotFoundWhenAttemptingToUpdateNonExistingChannel()
      throws IOException, ApiGatewayException {
    var request = createRequest(randomUUID().toString(), validRequestBody());

    doThrow(new NotFoundException(randomString())).when(client).updateChannel(any());

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_NOT_FOUND, response.getStatusCode());
  }

  @Test
  void shouldReturnBadGatewayWhenUnexpectedErrorFromChannelRegistry()
      throws IOException, ApiGatewayException {
    var request = createRequest(randomUUID().toString(), validRequestBody());

    doThrow(new BadGatewayException(randomString())).when(client).updateChannel(any());

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_BAD_GATEWAY, response.getStatusCode());
  }

  @Test
  void shouldReturnAcceptedWhenSuccessfullyUpdatedChannel() throws IOException {
    var request = createRequest(randomUUID().toString(), validRequestBody());

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_ACCEPTED, response.getStatusCode());
  }

  private static UpdateSerialPublicationRequest validRequestBody() {
    return new UpdateSerialPublicationRequest(randomString(), null, null);
  }

  private InputStream createRequestWithoutPathParams() throws JsonProcessingException {
    return new HandlerRequestBuilder<UpdateChannelRequest>(dtoObjectMapper)
        .withAccessRights(randomUri(), MANAGE_CUSTOMERS)
        .withBody(validRequestBody())
        .build();
  }

  private InputStream createRequest(String identifier, UpdateChannelRequest body)
      throws JsonProcessingException {
    return new HandlerRequestBuilder<UpdateChannelRequest>(dtoObjectMapper)
        .withAccessRights(randomUri(), AccessRight.MANAGE_CUSTOMERS)
        .withBody(body)
        .withPathParameters(Map.of(IDENTIFIER, identifier))
        .build();
  }

  private InputStream createRequestForUserWithoutAccessRights() throws JsonProcessingException {
    return new HandlerRequestBuilder<UpdateChannelRequest>(dtoObjectMapper)
        .withBody(validRequestBody())
        .withPathParameters(Map.of(IDENTIFIER, randomUUID().toString()))
        .withAccessRights(randomUri(), AccessRight.MANAGE_NVI)
        .build();
  }

  private InputStream createUnauthorizedRequest() throws JsonProcessingException {
    return new HandlerRequestBuilder<UpdateChannelRequest>(dtoObjectMapper)
        .withBody(validRequestBody())
        .withPathParameters(Map.of(IDENTIFIER, randomUUID().toString()))
        .build();
  }
}
