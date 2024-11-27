package no.sikt.nva.pubchannels.handler.create.serialpublication;

import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIAL_PUBLICATION_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import java.io.IOException;
import java.net.HttpURLConnection;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.create.BaseCreateSerialPublicationHandlerTest;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequestBuilder;
import no.sikt.nva.pubchannels.handler.create.series.CreateSeriesHandler;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class CreateSerialPublicationHandlerTest extends BaseCreateSerialPublicationHandlerTest {

    @Override
    protected CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> createHandler(Environment environment,
                                                                                                ChannelRegistryClient channelRegistryClient) {
        return new CreateSerialPublicationHandler(environment, channelRegistryClient);
    }

    @BeforeEach
    void setUp() {
        handlerUnderTest = new CreateSerialPublicationHandler(environment, publicationChannelClient);
        baseUri = UriWrapper.fromHost(environment.readEnv("API_DOMAIN"))
                      .addChild(CUSTOM_DOMAIN_BASE_PATH)
                      .addChild(SERIAL_PUBLICATION_PATH)
                      .getUri();
        channelRegistryCreatePathElement = "/createseries/";
        channelRegistryFetchPathElement = "/findseries/";
        type = SERIES_TYPE;
        customChannelPath = SERIES_PATH;
    }

    @Test
    void shouldReturnBadRequestWhenTypeIsInvalid() throws IOException {
        var requestBody = new CreateSerialPublicationRequestBuilder().withName("someName").withType("invalid").build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);
        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(containsString("Type must be either 'Journal' or 'Series'")));
    }
}
