package no.sikt.nva.pubchannels.handler.delete;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.UUID.randomUUID;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelDeleteClient;
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

class DeletePublicationChannelHandlerTest {

  private static final FakeContext CONTEXT = new FakeContext();
  private static final String IDENTIFIER = "identifier";
  private static final String BACKEND_SCOPE = "https://api.nva.unit.no/scopes/backend";
  private DeletePublicationChannelHandler handler;
  private PublicationChannelDeleteClient client;
  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    this.output = new ByteArrayOutputStream();
    this.client = mock(PublicationChannelClient.class);
    this.handler = new DeletePublicationChannelHandler(client);
  }

  @Test
  void shouldReturnUnauthorizedWhenRequestIsNotGatewayAuthorized() throws IOException {
    var request = createUnauthorizedRequest();

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void shouldReturnForbiddenWhenClientIsNotInternalBackend() throws IOException {
    var request = createUserRequest(randomUUID().toString());

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_FORBIDDEN, response.getStatusCode());
  }

  @Test
  void shouldReturnBadRequestWhenIdentifierIsMissingInPathParam() throws IOException {
    var request = createBackendRequestWithoutPathParams();

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldReturnBadRequestWhenIdentifierIsNotValidUuid() throws IOException {
    var request = createBackendRequest(randomString());

    handler.handleRequest(request, output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldReturnNotFoundWhenChannelDoesNotExist() throws IOException, ApiGatewayException {
    var identifier = randomUUID().toString();
    doThrow(new NotFoundException(randomString())).when(client).deleteChannel(eq(identifier));

    handler.handleRequest(createBackendRequest(identifier), output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_NOT_FOUND, response.getStatusCode());
  }

  @Test
  void shouldReturnBadGatewayWhenUpstreamFails() throws IOException, ApiGatewayException {
    var identifier = randomUUID().toString();
    doThrow(new BadGatewayException(randomString())).when(client).deleteChannel(eq(identifier));

    handler.handleRequest(createBackendRequest(identifier), output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertEquals(HTTP_BAD_GATEWAY, response.getStatusCode());
  }

  @Test
  void shouldReturnNoContentAndCallClientWhenDeleteSucceeds()
      throws IOException, ApiGatewayException {
    var identifier = randomUUID().toString();

    handler.handleRequest(createBackendRequest(identifier), output, CONTEXT);

    var response = GatewayResponse.fromOutputStream(output, Void.class);

    assertEquals(HTTP_NO_CONTENT, response.getStatusCode());
    verify(client).deleteChannel(identifier);
  }

  private static InputStream createBackendRequest(String identifier)
      throws JsonProcessingException {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withScope(BACKEND_SCOPE)
        .withPathParameters(Map.of(IDENTIFIER, identifier))
        .build();
  }

  private static InputStream createBackendRequestWithoutPathParams()
      throws JsonProcessingException {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper).withScope(BACKEND_SCOPE).build();
  }

  private static InputStream createUserRequest(String identifier) throws JsonProcessingException {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withAccessRights(randomUri(), AccessRight.MANAGE_CUSTOMERS)
        .withPathParameters(Map.of(IDENTIFIER, identifier))
        .build();
  }

  private static InputStream createUnauthorizedRequest() throws JsonProcessingException {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withPathParameters(Map.of(IDENTIFIER, randomUUID().toString()))
        .build();
  }
}
