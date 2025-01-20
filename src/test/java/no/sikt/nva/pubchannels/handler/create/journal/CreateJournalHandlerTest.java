package no.sikt.nva.pubchannels.handler.create.journal;

import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import static no.sikt.nva.pubchannels.handler.TestChannel.createEmptyTestChannel;
import static no.sikt.nva.pubchannels.handler.TestUtils.currentYearAsInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
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

class CreateJournalHandlerTest extends BaseCreateSerialPublicationHandlerTest {

    @Override
    protected CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> createHandler(
        Environment environment, ChannelRegistryClient channelRegistryClient) {
        return new CreateJournalHandler(environment, channelRegistryClient);
    }

    @BeforeEach
    void setUp() {
        handlerUnderTest = new CreateJournalHandler(environment, publicationChannelClient);
        baseUri =
            UriWrapper
                .fromHost(environment.readEnv("API_DOMAIN"))
                .addChild(CUSTOM_DOMAIN_BASE_PATH)
                .addChild(JOURNAL_PATH)
                .getUri();
        channelRegistryCreatePathElement = "/createjournal/";
        channelRegistryFetchPathElement = "/findjournal/";
        type = JOURNAL_TYPE;
        customChannelPath = JOURNAL_PATH;
    }

    @Test
    void shouldNotRequireTypeAsInput() throws IOException {
        var expectedPid = UUID
                              .randomUUID()
                              .toString();

        var clientRequest =
            new ChannelRegistryCreateSerialPublicationRequest(VALID_NAME, null, null, null);
        stubPostResponse(
            expectedPid,
            clientRequest,
            HttpURLConnection.HTTP_CREATED,
            channelRegistryCreatePathElement);

        var testChannel =
            createEmptyTestChannel(currentYearAsInteger(), expectedPid, type).withName(VALID_NAME);
        stubFetchOKResponse(testChannel, channelRegistryFetchPathElement);

        var requestBody = new CreateSerialPublicationRequestBuilder()
                              .withName(VALID_NAME)
                              .build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }
}
