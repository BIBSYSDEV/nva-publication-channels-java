package no.sikt.nva.pubchannels.handler.create.journal;

import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.create.BaseCreateSerialPublicationHandlerTest;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;

class CreateJournalHandlerTest extends BaseCreateSerialPublicationHandlerTest {

    @Override
    protected CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> createHandler(Environment environment,
                                                                                                ChannelRegistryClient channelRegistryClient) {
        return new CreateJournalHandler(environment, channelRegistryClient);
    }

    @BeforeEach
    void setUp() {
        handlerUnderTest = new CreateJournalHandler(environment, publicationChannelClient);
        baseUri = UriWrapper.fromHost(environment.readEnv("API_DOMAIN"))
                      .addChild(CUSTOM_DOMAIN_BASE_PATH)
                      .addChild(JOURNAL_PATH)
                      .getUri();
        channelRegistryCreatePathElement = "/createjournal/";
        channelRegistryFetchPathElement = "/findjournal/";
        type = JOURNAL_TYPE;
        customChannelPath = JOURNAL_PATH;
    }
}
