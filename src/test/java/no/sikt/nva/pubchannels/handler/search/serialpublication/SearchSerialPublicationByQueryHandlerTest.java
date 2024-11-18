package no.sikt.nva.pubchannels.handler.search.serialpublication;

import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;

import no.unit.nva.stubs.FakeContext;

import nva.commons.apigateway.GatewayResponse;

import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

class SearchSerialPublicationByQueryHandlerTest {

    private static final Context context = new FakeContext();
    private final SearchSerialPublicationByQueryHandler handlerUnderTest =
            new SearchSerialPublicationByQueryHandler();
    private ByteArrayOutputStream output;

    @Test
    void shouldReturnBadRequestWhenOffsetAndSizeAreNotDivisible() throws IOException {
        // Create request with offset and size that are not divisible
        var input =
                constructRequest(
                        Map.of(
                                "year",
                                String.valueOf(randomYear()),
                                "query",
                                randomString(),
                                "offset",
                                "5",
                                "size",
                                "8"),
                        MediaType.ANY_TYPE);
        // Call handler
        this.handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        // Verify that response is bad request
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Offset")));
    }
}
