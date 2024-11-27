package no.sikt.nva.pubchannels.handler.create.series;

import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_TYPE;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.create.BaseCreateSerialPublicationHandlerTest;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;

class CreateSeriesHandlerTest extends BaseCreateSerialPublicationHandlerTest {

    @Override
    protected CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> createHandler(Environment environment,
                                                                                                ChannelRegistryClient channelRegistryClient) {
        return new CreateSeriesHandler(environment, channelRegistryClient);
    }

    @BeforeEach
    void setUp() {
        handlerUnderTest = new CreateSeriesHandler(environment, publicationChannelClient);
        baseUri = UriWrapper.fromHost(environment.readEnv("API_DOMAIN"))
                      .addChild(CUSTOM_DOMAIN_BASE_PATH)
                      .addChild(SERIES_PATH)
                      .getUri();
        channelRegistryCreatePathElement = "/createseries/";
        channelRegistryFetchPathElement = "/findseries/";
        type = SERIES_TYPE;
        customChannelPath = SERIES_PATH;
    }
}
