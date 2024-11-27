package no.sikt.nva.pubchannels.handler.create;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.HttpURLConnection;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateJournalRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.handler.create.journal.CreateSerialPublicationRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

public abstract class BaseCreateSerialPublicationHandlerTest extends CreateHandlerTest {

    protected String channelRegistryPathElement;

    @Test
    void shouldThrowUnauthorizedIfNotUser() throws IOException {
        var requestBody = new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build();
        handlerUnderTest.handleRequest(constructUnauthorizedRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_UNAUTHORIZED)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Unauthorized")));
    }

    @Test
    void shouldReturnBadGatewayWhenUnauthorized() throws IOException {
        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());
        var request = new ChannelRegistryCreateJournalRequest(VALID_NAME, null, null, null);

        stubPostResponse(null, request, HttpURLConnection.HTTP_UNAUTHORIZED, channelRegistryPathElement);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnBadRequestWithOriginalErrorMessageWhenBadRequestFromChannelRegisterApi() throws IOException {
        var input = constructRequest(new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build());
        var request = new ChannelRegistryCreateJournalRequest(VALID_NAME, null, null, null);

        stubBadRequestResponse(request, channelRegistryPathElement);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        assertThat(response.getBodyObject(Problem.class).getDetail(), containsString(PROBLEM));
    }

    private static void stubPostResponse(String expectedPid,
                                         ChannelRegistryCreateJournalRequest request,
                                         int clientResponseHttpCode, String channelRegistryPathElement)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(clientResponseHttpCode,
                     channelRegistryPathElement + "createpid",
                     dtoObjectMapper.writeValueAsString(new CreateChannelResponse(expectedPid)),
                     dtoObjectMapper.writeValueAsString(request));
    }

    private static void stubBadRequestResponse(ChannelRegistryCreateJournalRequest request,
                                               String channelRegistryPathElement)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(HttpURLConnection.HTTP_BAD_REQUEST,
                     channelRegistryPathElement + "createpid",
                     dtoObjectMapper.writeValueAsString(PROBLEM),
                     dtoObjectMapper.writeValueAsString(request));
    }
}
