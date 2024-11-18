package no.sikt.nva.pubchannels.handler.search.serialpublication;

import static no.sikt.nva.pubchannels.TestCommons.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestCommons.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestCommons.SERIAL_PUBLICATION_PATH;
import static no.sikt.nva.pubchannels.TestCommons.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.net.MediaType;

import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;

import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

@WireMockTest(httpsEnabled = true)
class SearchSerialPublicationByQueryHandlerTest {

    private static final Context context = new FakeContext();
    private static final String SELF_URI_BASE =
            "https://localhost/publication-channels/" + SERIAL_PUBLICATION_PATH;
    private SearchSerialPublicationByQueryHandler handlerUnderTest;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {

        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);
        var channelRegistryBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient =
                new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);

        this.handlerUnderTest =
                new SearchSerialPublicationByQueryHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#invalidYearsProvider")
    void shouldReturnBadRequestWhenYearIsInvalid(String year) throws IOException {
        var queryParameters = Map.of("year", year, "query", "asd");
        var input = constructRequest(queryParameters, MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Year")));
    }

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
