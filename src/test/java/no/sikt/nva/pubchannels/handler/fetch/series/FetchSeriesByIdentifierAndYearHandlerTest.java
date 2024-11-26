package no.sikt.nva.pubchannels.handler.fetch.series;

import static no.sikt.nva.pubchannels.TestConstants.SERIES_PATH;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockChannelRegistryResponse;
import java.net.URI;
import java.util.Map;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.fetch.FetchSerialPublicationByIdentifierAndYearHandlerTest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import org.junit.jupiter.api.BeforeEach;

class FetchSeriesByIdentifierAndYearHandlerTest extends FetchSerialPublicationByIdentifierAndYearHandlerTest {

    private static final String SERIES_IDENTIFIER_FROM_CACHE = "50561B90-6679-4FCD-BCB0-99E521B18962";
    private static final String SERIES_YEAR_FROM_CACHE = "2024";
    private static final URI SELF_URI_BASE = URI.create("https://localhost/publication-channels/" + SERIES_PATH);
    private static final String CHANNEL_REGISTRY_PATH_ELEMENT = "/findseries/";
    private static final String SERIES_PATH_ELEMENT = "series";

    @Override
    protected String getChannelRegistryPathElement() {
        return CHANNEL_REGISTRY_PATH_ELEMENT;
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> createHandler(
        ChannelRegistryClient publicationChannelClient) {
        return new FetchSeriesByIdentifierAndYearHandler(environment, publicationChannelClient, cacheService,
                                                         super.getAppConfigWithCacheEnabled(false));
    }

    @Override
    protected SerialPublicationDto mockChannelFound(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Series");
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    @Override
    protected SerialPublicationDto mockChannelFoundYearValueNull(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Series")
                              .withScientificValueReviewNotice(Map.of("en", "This is a review notice",
                                                                      "no", "Vedtak"));
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    @Override
    protected SerialPublicationDto mockChannelWithScientificValueReviewNotice(int year, String identifier) {
        var testChannel = new TestChannel(year, identifier, "Series")
                              .withScientificValueReviewNotice(Map.of("en", "This is a review notice",
                                                                      "no", "Vedtak"));
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return testChannel.asSerialPublicationDto(SELF_URI_BASE, String.valueOf(year));
    }

    @Override
    protected void mockChannelFound(int year, String identifier, String channelRegistryResponseBody) {
        mockChannelRegistryResponse(CHANNEL_REGISTRY_PATH_ELEMENT, String.valueOf(year), identifier,
                                    channelRegistryResponseBody);
    }

    @Override
    protected TestChannel generateTestChannel(int year, String identifier) {
        return new TestChannel(year, identifier, "Series");
    }

    @Override
    protected String getPath() {
        return SERIES_PATH_ELEMENT;
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment,
                                                                          this.channelRegistryClient,
                                                                          this.cacheService,
                                                                          super.getAppConfigWithCacheEnabled(false));
    }
}
