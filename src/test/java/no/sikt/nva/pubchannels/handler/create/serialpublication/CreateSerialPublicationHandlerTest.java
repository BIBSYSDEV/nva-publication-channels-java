package no.sikt.nva.pubchannels.handler.create.serialpublication;

import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.SERIAL_PUBLICATION_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_TYPE;
import static no.sikt.nva.pubchannels.handler.TestChannel.createEmptyTestChannel;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.currentYear;
import static no.sikt.nva.pubchannels.handler.TestUtils.currentYearAsInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.create.BaseCreateSerialPublicationHandlerTest;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequestBuilder;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
        channelRegistryFetchPathElement = "/findjournalserie/";
        type = SERIES_TYPE;
        customChannelPath = SERIAL_PUBLICATION_PATH;
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

    @ParameterizedTest(name = "Should create new channel for type: {0}")
    @EnumSource(value = ChannelType.class, names = {"JOURNAL", "SERIES"})
    void shouldCreateNewChannel(ChannelType type) throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        var channelRegistryRequest = new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, null, null);
        stubPostResponse(expectedPid, channelRegistryRequest, HttpURLConnection.HTTP_CREATED,
                         channelRegistryCreatePathElement);

        var typeName = ChannelType.JOURNAL.equals(type) ? JOURNAL_TYPE : SERIES_TYPE;
        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, typeName).withName(
            VALID_NAME);
        stubFetchOKResponse(testChannel, channelRegistryFetchPathElement);

        var requestBody = requestBuilderWithRequiredFields().build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation,
                   is(equalTo(createPublicationChannelUri(expectedPid, customChannelPath, currentYear()))));

        var expectedChannel = testChannel.asSerialPublicationDto(baseUri, currentYear());
        assertThat(response.getBodyObject(SerialPublicationDto.class), is(equalTo(expectedChannel)));
    }
}
