package no.sikt.nva.pubchannels.handler.fetch.serialpublication;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.SERIAL_PUBLICATION_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_TYPE;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import com.google.common.net.MediaType;
import java.io.IOException;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.fetch.BaseFetchSerialPublicationByIdentifierAndYearHandlerTest;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FetchSerialPublicationByIdentifierAndYearHandlerTest extends
                                                                  BaseFetchSerialPublicationByIdentifierAndYearHandlerTest {

    @Override
    protected FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> createHandler(
        ChannelRegistryClient publicationChannelClient) {
        return new FetchSerialPublicationByIdentifierAndYearHandler(environment, publicationChannelClient,
                                                                    cacheService,
                                                                    super.getAppConfigWithCacheEnabled(false));
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, ?> createHandler(Environment environment,
                                                                     PublicationChannelClient publicationChannelClient,
                                                                     CacheService cacheService,
                                                                     AppConfig appConfigWithCacheEnabled) {
        return new FetchSerialPublicationByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                                    appConfigWithCacheEnabled);
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchSerialPublicationByIdentifierAndYearHandler(environment,
                                                                                     this.channelRegistryClient,
                                                                                     this.cacheService,
                                                                                     super.getAppConfigWithCacheEnabled(
                                                                                         false));
        this.type = JOURNAL_TYPE;
        this.customChannelPath = SERIAL_PUBLICATION_PATH;
        this.selfBaseUri = UriWrapper.fromHost(API_DOMAIN)
                                     .addChild(CUSTOM_DOMAIN_BASE_PATH)
                                     .addChild(SERIAL_PUBLICATION_PATH)
                                     .getUri();
        this.channelRegistryPathElement = "/findjournalserie/";
    }

    @ParameterizedTest(name = "Should return correct data for type \"{0}\"")
    @ValueSource(strings = {JOURNAL_TYPE, SERIES_TYPE})
    void shouldReturnCorrectDataWithSuccessWhenExists(String type) throws IOException {
        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var expectedChannel = mockChannelFoundAndReturnExpectedResponse(year, identifier, type);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedChannel)));
    }
}
