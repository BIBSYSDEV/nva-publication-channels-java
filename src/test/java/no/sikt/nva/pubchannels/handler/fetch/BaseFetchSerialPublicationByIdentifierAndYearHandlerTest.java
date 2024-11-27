package no.sikt.nva.pubchannels.handler.fetch;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.HARDCODED_CACHED_TITLE;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockChannelRegistryResponse;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockResponseWithHttpStatus;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.zalando.problem.Problem;

/**
 * Common behavior for FetchSeriesByIdentifierAndYearHandler, FetchSerialPublicationByIdentifierAndYearHandler, and
 * FetchJournalByIdentifierAndYearHandler is tested here
 */
public abstract class BaseFetchSerialPublicationByIdentifierAndYearHandlerTest
    extends FetchByIdentifierAndYearHandlerTest {

    private static final String JOURNAL_IDENTIFIER_FROM_CACHE = "50561B90-6679-4FCD-BCB0-99E521B18962";
    private static final String JOURNAL_YEAR_FROM_CACHE = "2024";
    protected String type;
    protected URI selfBaseUri;

    protected abstract FetchByIdentifierAndYearHandler<Void, ?> createHandler(Environment environment,
                                                                              PublicationChannelClient publicationChannelClient,
                                                                              CacheService cacheService,
                                                                              AppConfig appConfigWithCacheEnabled);

    protected SerialPublicationDto mockChannelFoundAndReturnExpectedResponse(String year, String identifier,
                                                                             String type) {
        var testChannel = new TestChannel(year, identifier, type);
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelFoundWithBody(year, identifier, body);

        return testChannel.asSerialPublicationDto(selfBaseUri, year);
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
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
    void shouldIncludeYearInResponse() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);
        mockChannelFoundAndReturnExpectedResponse(year, identifier, type);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualYear = response.getBodyObject(SerialPublicationDto.class).year();
        assertThat(actualYear, is(equalTo(year)));
    }

    @ParameterizedTest(name = "Should return requested media type \"{0}\"")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType) throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier, mediaType);

        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedSeries = mockChannelFoundAndReturnExpectedResponse(year, identifier, type);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));
        var contentType = response.getHeaders().get(CONTENT_TYPE);
        assertThat(contentType, is(equalTo(expectedMediaType)));
    }

    @Test
    void shouldReturnChannelIdWithRequestedYearIfThirdPartyDoesNotProvideYear() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var expectedSeries = mockChannelFoundYearValueNull(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
    }

    @Test
    void shouldIncludeScientificReviewNoticeWhenLevelDisplayX() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);
        var expectedSeries = mockChannelWithScientificValueReviewNotice(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualReviewNotice = response.getBodyObject(SerialPublicationDto.class).reviewNotice();
        assertThat(actualReviewNotice, is(equalTo(expectedSeries.reviewNotice())));
    }

    @Test
    void shouldNotFailWhenChannelRegistryLevelNull() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var testChannel = generateTestChannel(year, identifier);
        mockChannelFoundWithBody(year, identifier, testChannel.asChannelRegistrySeriesBodyWithoutLevel());

        handlerUnderTest.handleRequest(constructRequest(year, identifier, MediaType.ANY_TYPE), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertEquals(HTTP_OK, response.getStatusCode());
    }

    @Test
    void shouldReturnChannelWhenChannelRegistryIsUnavailableAndChannelIsCached() throws IOException {
        var identifier = JOURNAL_IDENTIFIER_FROM_CACHE;
        var year = JOURNAL_YEAR_FROM_CACHE;

        mockResponseWithHttpStatus(channelRegistryPathElement, identifier, year, HTTP_INTERNAL_ERROR);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        super.loadAndEnableCache();
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualTitle = response.getBodyObject(SerialPublicationDto.class).name();
        assertThat(actualTitle, is(equalTo(HARDCODED_CACHED_TITLE)));

        assertThat(response.getStatusCode(), is(equalTo(HTTP_OK)));
    }

    @Test
    void shouldReturnChannelFromCacheWhenShouldUseCacheEnvironmentVariableIsTrue() throws IOException {

        var input = constructRequest(JOURNAL_YEAR_FROM_CACHE, JOURNAL_IDENTIFIER_FROM_CACHE, MediaType.ANY_TYPE);

        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = createHandler(environment, channelRegistryClient, cacheService,
                                              super.getAppConfigWithCacheEnabled(true));

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualTitle = response.getBodyObject(SerialPublicationDto.class).name();
        assertThat(actualTitle, is(equalTo(HARDCODED_CACHED_TITLE)));

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));
    }

    @Test
    void shouldReturnNotFoundWhenShouldUseCacheEnvironmentVariableIsTrueButChannelIsNotCached() throws IOException {
        when(environment.readEnv("SHOULD_USE_CACHE")).thenReturn("true");
        super.loadAndEnableCache();
        this.handlerUnderTest = createHandler(environment, channelRegistryClient, cacheService,
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

    private TestChannel generateTestChannel(String year, String identifier) {
        return new TestChannel(year, identifier, type);
    }

    private SerialPublicationDto mockChannelFoundYearValueNull(String year, String identifier) {
        var testChannel = new TestChannel(year, identifier, type)
                              .withScientificValueReviewNotice(Map.of("en", "This is a review notice",
                                                                      "no", "Vedtak"));
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(channelRegistryPathElement, year, identifier, body);

        return testChannel.asSerialPublicationDto(selfBaseUri, year);
    }

    private SerialPublicationDto mockChannelWithScientificValueReviewNotice(String year, String identifier) {
        var testChannel = new TestChannel(year, identifier, type)
                              .withScientificValueReviewNotice(Map.of("en", "This is a review notice",
                                                                      "no", "Vedtak"));
        var body = testChannel.asChannelRegistrySeriesBody();

        mockChannelRegistryResponse(channelRegistryPathElement, String.valueOf(year), identifier, body);

        return testChannel.asSerialPublicationDto(selfBaseUri, String.valueOf(year));
    }

    private void mockChannelFoundWithBody(String year, String identifier, String channelRegistryResponseBody) {
        mockChannelRegistryResponse(channelRegistryPathElement, year, identifier, channelRegistryResponseBody);
    }
}
