package no.sikt.nva.pubchannels.handler.fetch;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.junit.jupiter.api.Test;

class RequestObjectTest {

  @Test
  void shouldReturnUpperCaseIdentifierWhenCreatingRequestObjectFromRequestInfo()
      throws ApiGatewayException {
    var requestInfo = mock(RequestInfo.class);
    var identifier = randomUUID().toString();
    when(requestInfo.getPathParameter("identifier")).thenReturn(identifier);
    when(requestInfo.getPathParameter("type")).thenReturn("publisher");

    var requestObject = RequestObject.fromRequestInfo(requestInfo);

    assertEquals(identifier.toUpperCase(Locale.getDefault()), requestObject.identifier());
  }
}
