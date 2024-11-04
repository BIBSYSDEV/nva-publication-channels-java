package no.sikt.nva.pubchannels.handler.fetch.journal;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestCommons.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.sikt.nva.pubchannels.TestCommons.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestCommons.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestCommons.JOURNAL_PATH;
import static no.sikt.nva.pubchannels.TestCommons.LOCATION;
import static no.sikt.nva.pubchannels.TestCommons.WILD_CARD;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.net.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.TestUtils;
import no.sikt.nva.pubchannels.handler.model.JournalDto;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
class FetchJournalByIdentifierAndYearHandlerTest {

    private static final int YEAR_START = 2004;
    private static final Context context = new FakeContext();
    private FetchJournalByIdentifierAndYearHandler handlerUnderTest;
    private PublicationChannelMockClient mockRegistry;
    private ByteArrayOutputStream output;
    private Environment environment;
    private String channelRegistryBaseUri;

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {

        this.environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);

        channelRegistryBaseUri = runtimeInfo.getHttpsBaseUrl();
        var httpClient = WiremockHttpClient.create();
        var publicationChannelSource = new ChannelRegistryClient(httpClient, URI.create(channelRegistryBaseUri), null);
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource);
        this.mockRegistry = new PublicationChannelMockClient();
        this.output = new ByteArrayOutputStream();
    }

    @ParameterizedTest
    @DisplayName("Should return requested media type")
    @MethodSource("no.sikt.nva.pubchannels.TestCommons#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType) throws IOException {
        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var year = TestUtils.randomYear();
        var identifier = mockRegistry.randomJournal(year);
        var input = constructRequest(String.valueOf(year), identifier, mediaType);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, JournalDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));
        var contentType = response.getHeaders().get(CONTENT_TYPE);
        assertThat(contentType, is(equalTo(expectedMediaType)));

        var actualJournal = response.getBodyObject(JournalDto.class);
        var expectedJournal = mockRegistry.getJournal(identifier);
        assertThat(actualJournal, is(equalTo(expectedJournal)));
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = TestUtils.randomYear();
        var identifier = mockRegistry.randomJournal(year);
        var input = constructRequest(String.valueOf(year), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, JournalDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualJournal = response.getBodyObject(JournalDto.class);
        var expectedJournal = mockRegistry.getJournal(identifier);
        assertThat(actualJournal, is(equalTo(expectedJournal)));
    }

    @Test
    void shouldIncludeYearInResponse() throws IOException {
        var year = TestUtils.randomYear();
        var identifier = mockRegistry.randomJournal(year);
        var input = constructRequest(String.valueOf(year), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, JournalDto.class);
        var actualYear = response.getBodyObject(JournalDto.class).year();
        assertThat(actualYear, is(equalTo(String.valueOf(year))));
    }

    @Test
    void shouldNotFailWhenChannelRegistryLevelNull() throws IOException {
        var year = TestUtils.randomYear();
        var identifier = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, identifier);

        mockRegistry.mockChannelRegistry(year, testChannel, testChannel.asChannelRegistryJournalBodyWithoutLevel());
        var input = constructRequest(String.valueOf(year), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, JournalDto.class);
        assertEquals(HTTP_OK, response.getStatusCode());
    }

    @Test
    void shouldIncludeScientificReviewNoticeWhenLevelDisplayX() throws IOException {
        var year = TestUtils.randomYear();
        var identifier = mockRegistry.journalWithScientificValueReviewNotice(year);
        var input = constructRequest(String.valueOf(year), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, JournalDto.class);
        var actualJournalReviewNotice = response.getBodyObject(JournalDto.class).reviewNotice();
        var expectedJournalReviewNotice = mockRegistry.getJournal(identifier).reviewNotice();
        assertThat(actualJournalReviewNotice, is(equalTo(expectedJournalReviewNotice)));
    }

    @Test
    void shouldNotIncludeScientificReviewNoticeWhenLevelDisplayNotX() throws IOException {
        var year = TestUtils.randomYear();
        var identifier = mockRegistry.randomJournal(year);
        var input = constructRequest(String.valueOf(year), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, JournalDto.class);
        var actualJournal = response.getBodyObject(JournalDto.class);
        assertNull(actualJournal.reviewNotice());
    }

    @Test
    void shouldReturnChannelIdWithRequestedYearIfThirdPartyDoesNotProvideYear() throws IOException {
        var year = TestUtils.randomYear();
        var identifier = mockRegistry.randomJournalWithThirdPartyYearValueNull(year);
        var input = constructRequest(String.valueOf(year), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, JournalDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualJournal = response.getBodyObject(JournalDto.class);
        var expectedJournal = mockRegistry.getJournal(identifier);
        assertThat(actualJournal, is(equalTo(expectedJournal)));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("invalidYearsProvider")
    void shouldReturnBadRequestWhenPathParameterYearIsNotValid(String year) throws IOException {

        var input = constructRequest(year, UUID.randomUUID().toString());

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Year")));
    }

    @ParameterizedTest(name = "identifier \"{0}\" is invalid")
    @ValueSource(strings = {" ", "abcd", "ab78ab78ab78ab78ab78a7ba87b8a7ba87b8"})
    void shouldReturnBadRequestWhenPathParameterIdentifierIsNotValid(String identifier) throws IOException {

        var input = constructRequest(randomYear(), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Pid")));
    }

    @Test
    void shouldReturnBadGatewayWhenChannelRegistryIsUnavailable() throws IOException {
        var httpClient = WiremockHttpClient.create();
        var channelRegistryBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource);

        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        var input = constructRequest(year, identifier);

        var appender = LogUtils.getTestingAppenderForRootLogger();

        handlerUnderTest.handleRequest(input, output, context);

        var upstreamUrl = "https://localhost:9898/findjournal/" + identifier + "/" + year;
        assertThat(appender.getMessages(), containsString("Unable to reach upstream: " + upstreamUrl));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unable to reach upstream!")));
    }

    @Test
    void shouldLogErrorAndReturnBadGatewayWhenInterruptionOccurs() throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        when(httpClient.send(any(), any())).thenThrow(new InterruptedException());
        var channelRegistryBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, null);

        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource);

        var input = constructRequest(randomYear(), UUID.randomUUID().toString());

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Unable to reach upstream!"));
        assertThat(appender.getMessages(), containsString(InterruptedException.class.getSimpleName()));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(equalTo("Unable to reach upstream!")));
    }

    @Test
    void shouldReturnNotFoundWhenExternalApiRespondsWithNotFound() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        mockRegistry.notFoundJournal(identifier, year);

        var input = constructRequest(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_NOT_FOUND)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Publication channel not found!")));
    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelRegistryReturnsUnhandledResponseCode() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        mockRegistry.internalServerErrorJournal(identifier, year);

        var input = constructRequest(year, identifier);

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Error fetching publication channel"));
        assertThat(appender.getMessages(), containsString("500"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldReturnRedirectWhenChannelRegistryReturnsRedirect() throws IOException {
        var year = randomYear();
        var requestedIdentifier = UUID.randomUUID().toString();
        var newIdentifier = UUID.randomUUID().toString();
        var newChannelRegistryLocation = UriWrapper.fromHost(channelRegistryBaseUri)
                                             .addChild("findjournal", newIdentifier, year)
                                             .toString();
        mockRegistry.redirect(requestedIdentifier, newChannelRegistryLocation, year);
        handlerUnderTest.handleRequest(constructRequest(year, requestedIdentifier), output, context);
        var response = GatewayResponse.fromOutputStream(output, HttpResponse.class);
        assertEquals(HttpURLConnection.HTTP_MOVED_PERM, response.getStatusCode());
        var expectedLocation = constructExpectedLocation(newIdentifier, year);
        assertEquals(expectedLocation, response.getHeaders().get(LOCATION));
        assertEquals(WILD_CARD, response.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private static String constructExpectedLocation(String newIdentifier, String year) {
        return UriWrapper.fromHost(API_DOMAIN)
                   .addChild(CUSTOM_DOMAIN_BASE_PATH, JOURNAL_PATH, newIdentifier, year)
                   .toString();
    }

    private static InputStream constructRequest(String year, String identifier, MediaType mediaType)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper).withHeaders(Map.of(ACCEPT, mediaType.toString()))
                   .withPathParameters(Map.of("identifier",
                                              identifier,
                                              "year",
                                              year))
                   .build();
    }

    private static InputStream constructRequest(String year, String identifier) throws JsonProcessingException {
        return constructRequest(year, identifier, MediaType.JSON_UTF_8);
    }

    private static Stream<String> invalidYearsProvider() {
        String yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", yearAfterNextYear, "21000");
    }

    private String randomYear() {
        var bound = (LocalDate.now().getYear() + 1) - YEAR_START;
        return Integer.toString(YEAR_START + randomInteger(bound));
    }
}
