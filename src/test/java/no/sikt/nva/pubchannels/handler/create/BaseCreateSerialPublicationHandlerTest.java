package no.sikt.nva.pubchannels.handler.create;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import java.io.IOException;
import java.net.HttpURLConnection;
import no.sikt.nva.pubchannels.handler.create.journal.CreateSerialPublicationRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

public abstract class BaseCreateSerialPublicationHandlerTest extends CreateHandlerTest {

    @Test
    void shouldThrowUnauthorizedIfNotUser() throws IOException {
        var requestBody = new CreateSerialPublicationRequestBuilder().withName(VALID_NAME).build();
        handlerUnderTest.handleRequest(constructUnauthorizedRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_UNAUTHORIZED)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Unauthorized")));
    }
}
