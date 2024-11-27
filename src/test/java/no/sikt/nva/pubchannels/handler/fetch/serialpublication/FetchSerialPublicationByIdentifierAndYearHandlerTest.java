package no.sikt.nva.pubchannels.handler.fetch.serialpublication;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.SERIAL_PUBLICATION_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_TYPE;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockResponseWithHttpStatus;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.fetch.BaseFetchSerialPublicationByIdentifierAndYearHandlerTest;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.fetch.series.FetchSeriesByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

public class FetchSerialPublicationByIdentifierAndYearHandlerTest extends
                                                                  BaseFetchSerialPublicationByIdentifierAndYearHandlerTest {

    private static final String SERIES_IDENTIFIER_FROM_CACHE = "50561B90-6679-4FCD-BCB0-99E521B18962";
    private static final String SERIES_YEAR_FROM_CACHE = "2024";
    private static final URI SELF_URI_BASE = URI.create(
        "https://localhost/publication-channels/" + SERIAL_PUBLICATION_PATH);
    private static final String CHANNEL_REGISTRY_PATH_ELEMENT = "/findjournalserie/";
    private static final String SERIAL_PUBLICATION_PATH_ELEMENT = "serial-publication";

    @Override
    protected String getChannelRegistryPathElement() {
        return CHANNEL_REGISTRY_PATH_ELEMENT;
    }

    @Override
    protected FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> createHandler(
        ChannelRegistryClient publicationChannelClient) {
        return new FetchSerialPublicationByIdentifierAndYearHandler(environment, publicationChannelClient,
                                                                    cacheService,
                                                                    super.getAppConfigWithCacheEnabled(false));
    }

    @Override
    protected URI getSelfBaseUri() {
        return SELF_URI_BASE;
    }

    @Override
    protected String getPath() {
        return SERIAL_PUBLICATION_PATH_ELEMENT;
    }

    @Override
    protected String getType() {
        return JOURNAL_TYPE;
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new FetchSerialPublicationByIdentifierAndYearHandler(environment,
                                                                                     this.channelRegistryClient,
                                                                                     this.cacheService,
                                                                                     super.getAppConfigWithCacheEnabled(
                                                                                         false));
    }

    @ParameterizedTest(name = "should return correct data for type {0}")
    @ValueSource(strings = {JOURNAL_TYPE, SERIES_TYPE})
    void shouldReturnCorrectDataWithSuccessWhenExists(String type) throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var expectedChannel = mockChannelFoundAndReturnExpectedResponse(year, identifier, type);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedChannel)));
    }

    @Test
    void shouldReturnSeriesWhenChannelRegistryIsUnavailableAndSeriesIsCached() throws IOException {
        var identifier = SERIES_IDENTIFIER_FROM_CACHE;
        var year = SERIES_YEAR_FROM_CACHE;

        mockResponseWithHttpStatus(CHANNEL_REGISTRY_PATH_ELEMENT, identifier, year, HTTP_INTERNAL_ERROR);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        super.loadAndEnableCache();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Fetching SERIAL_PUBLICATION from cache: " + identifier));

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_OK)));
    }

    @Test
    void shouldReturnSeriesFromCacheWhenShouldUseCacheEnvironmentVariableIsTrue() throws IOException {
        var input = constructRequest(SERIES_YEAR_FROM_CACHE, SERIES_IDENTIFIER_FROM_CACHE, MediaType.ANY_TYPE);

        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment, null, cacheService,
                                                                          super.getAppConfigWithCacheEnabled(true));
        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertThat(appender.getMessages(),
                   containsString("Fetching SERIES from cache: " + SERIES_IDENTIFIER_FROM_CACHE));

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));
    }

    @Test
    void shouldReturnNotFoundWhenShouldUseCacheEnvironmentVariableIsTrueButSeriesIsNotCached() throws IOException {
        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment, null, cacheService,
                                                                          super.getAppConfigWithCacheEnabled(true));

        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Could not find cached publication channel with"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HTTP_NOT_FOUND)));
    }
}
