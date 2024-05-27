package no.sikt.nva.pubchannels.handler.fetch.series;

import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestCommons.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.sikt.nva.pubchannels.TestCommons.LOCATION;
import static no.sikt.nva.pubchannels.TestCommons.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.createDataportenJournalResponse;
import static no.sikt.nva.pubchannels.handler.TestUtils.createSeries;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockDataportenResponse;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockRedirectedClient;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockResponseWithHttpStatus;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.sikt.nva.pubchannels.handler.TestUtils.setupInterruptedClient;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.net.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.TestUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.RandomDataGenerator;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
class FetchSeriesByIdentifierAndYearHandlerTest {

    public static final String SERIES_PATH = "series";
    private static final String SELF_URI_BASE = "https://localhost/publication-channels/" + SERIES_PATH;
    private static final String DATAPORTEN_PATH_ELEMENT = "/findseries/";
    private static final Context context = new FakeContext();
    private FetchSeriesByIdentifierAndYearHandler handlerUnderTest;
    private ByteArrayOutputStream output;
    private Environment environment;
    private String dataportenBaseUri;

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        dataportenBaseUri = runtimeInfo.getHttpsBaseUrl();
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new DataportenPublicationChannelClient(httpClient, URI.create(dataportenBaseUri),
                                                                              null);
        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        output.flush();
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);

        var expectedSeries = mockSeriesFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FetchByIdAndYearResponse.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualSeries = response.getBodyObject(FetchByIdAndYearResponse.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
    }

    @ParameterizedTest
    @DisplayName("Should return requested media type")
    @MethodSource("no.sikt.nva.pubchannels.TestCommons#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType) throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, mediaType);

        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedSeries = mockSeriesFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FetchByIdAndYearResponse.class);

        var actualSeries = response.getBodyObject(FetchByIdAndYearResponse.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));
        var contentType = response.getHeaders().get(CONTENT_TYPE);
        assertThat(contentType, is(equalTo(expectedMediaType)));
    }

    @Test
    void shouldReturnChannelIdWithRequestedYearIfThirdPartyDoesNotProvideYear() throws IOException {
        var year = String.valueOf(randomYear());
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var expectedSeries = mockSeriesFoundYearValueNull(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FetchByIdAndYearResponse.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualSeries = response.getBodyObject(FetchByIdAndYearResponse.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("invalidYearsProvider")
    void shouldReturnBadRequestWhenPathParameterYearIsNotValid(String year)
        throws IOException {

        var input = constructRequest(year, UUID.randomUUID().toString(), MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(containsString("Year")));
    }

    @ParameterizedTest(name = "identifier \"{0}\" is invalid")
    @ValueSource(strings = {" ", "abcd", "ab78ab78ab78ab78ab78a7ba87b8a7ba87b8"})
    void shouldReturnBadRequestWhenPathParameterIdentifierIsNotValid(String identifier)
        throws IOException {

        var input = constructRequest(String.valueOf(randomYear()), identifier, MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("Pid")));
    }

    @Test
    void shouldReturnNotFoundWhenExternalApiRespondsWithNotFound() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = String.valueOf(randomYear());

        mockResponseWithHttpStatus("/findseries/", identifier, year, HttpURLConnection.HTTP_NOT_FOUND);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_NOT_FOUND)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(equalTo("Publication channel not found!")));
    }

    @Test
    void shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCode() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = String.valueOf(randomYear());

        mockResponseWithHttpStatus("/findseries/", identifier, year,
                                   HttpURLConnection.HTTP_INTERNAL_ERROR);

        var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Error fetching publication channel: 500"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream!")));
    }

    @Test
    void shouldLogErrorAndReturnBadGatewayWhenInterruptionOccurs() throws IOException, InterruptedException {
        DataportenPublicationChannelClient publicationChannelClient = setupInterruptedClient();

        this.handlerUnderTest = new FetchSeriesByIdentifierAndYearHandler(environment, publicationChannelClient);

        var input = constructRequest(String.valueOf(randomYear()), UUID.randomUUID().toString(), MediaType.ANY_TYPE);

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
    void shouldReturnRedirectWhenChannelRegistryReturnsRedirect() throws IOException {
        var year = String.valueOf(randomYear());
        var requestedIdentifier = UUID.randomUUID().toString();
        var newIdentifier = UUID.randomUUID().toString();
        var newChannelRegistryLocation = UriWrapper.fromHost(dataportenBaseUri)
                                             .addChild(DATAPORTEN_PATH_ELEMENT, newIdentifier, year)
                                             .toString();
        mockRedirectedClient(requestedIdentifier, newChannelRegistryLocation, year, DATAPORTEN_PATH_ELEMENT);
        handlerUnderTest.handleRequest(constructRequest(year, requestedIdentifier, MediaType.ANY_TYPE), output,
                                       context);
        var response = GatewayResponse.fromOutputStream(output, HttpResponse.class);
        assertEquals(HttpURLConnection.HTTP_MOVED_PERM, response.getStatusCode());
        var expectedLocation = TestUtils.constructExpectedLocation(newIdentifier, year, SERIES_PATH);
        assertEquals(expectedLocation, response.getHeaders().get(LOCATION));
        assertEquals(WILD_CARD, response.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private static Stream<String> invalidYearsProvider() {
        String yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", yearAfterNextYear, "21000");
    }

    private FetchByIdAndYearResponse mockSeriesFound(int year, String identifier) {
        var name = randomString();
        var electronicIssn = randomIssn();
        var issn = randomIssn();
        var scientificValue = RandomDataGenerator.randomElement(ScientificValue.values());
        var level = TestUtils.scientificValueToLevel(scientificValue);
        var landingPage = randomUri();
        var yearString = String.valueOf(year);
        var body = createDataportenJournalResponse(year, name, identifier, electronicIssn, issn, landingPage, level);

        mockDataportenResponse(DATAPORTEN_PATH_ELEMENT, yearString, identifier, body);

        return getFetchByIdAndYearResponse(yearString, identifier, name, electronicIssn, issn, scientificValue,
                                           landingPage);
    }

    private FetchByIdAndYearResponse mockSeriesFoundYearValueNull(String year, String identifier) {
        var name = randomString();
        var electronicIssn = randomIssn();
        var issn = randomIssn();
        var scientificValue = RandomDataGenerator.randomElement(ScientificValue.values());
        var level = TestUtils.scientificValueToLevel(scientificValue);
        var landingPage = randomUri();
        var body = createDataportenJournalResponse(null, name, identifier, electronicIssn, issn, landingPage, level);

        mockDataportenResponse(DATAPORTEN_PATH_ELEMENT, year, identifier, body);

        return getFetchByIdAndYearResponse(year, identifier, name, electronicIssn, issn, scientificValue,
                                           landingPage);
    }

    private FetchByIdAndYearResponse getFetchByIdAndYearResponse(
        String year,
        String identifier,
        String name,
        String electronicIssn,
        String issn,
        ScientificValue scientificValue,
        URI landingPage) {

        var selfUriBase = URI.create(SELF_URI_BASE);
        var series = createSeries(
            year,
            identifier,
            name,
            electronicIssn,
            issn,
            scientificValue,
            landingPage);

        return FetchByIdAndYearResponse.create(selfUriBase, series, year);
    }
}
