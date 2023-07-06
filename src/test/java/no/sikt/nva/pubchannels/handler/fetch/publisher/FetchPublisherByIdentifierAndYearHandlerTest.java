package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static no.sikt.nva.pubchannels.handler.TestUtils.YEAR_START;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.createDataportenPublisherResponse;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublisher;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockDataportenResponse;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockResponseWithHttpStatus;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomIsbnPrefix;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.sikt.nva.pubchannels.handler.TestUtils.scientificValueToLevel;
import static no.sikt.nva.pubchannels.handler.TestUtils.setupInterruptedClient;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
class FetchPublisherByIdentifierAndYearHandlerTest {

    public static final String SELF_URI_BASE = "https://localhost/publication-channels/publisher";
    private static final String DATAPORTEN_PATH_ELEMENT = "/findpublisher/";
    private static final Context context = new FakeContext();
    private FetchPublisherByIdentifierAndYearHandler handlerUnderTest;
    private ByteArrayOutputStream output;
    private Environment environment;

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new DataportenPublicationChannelClient(httpClient, dataportenBaseUri, null);
        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment, publicationChannelClient);
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

        var input = constructRequest(String.valueOf(year), identifier);

        var expectedPublisher = mockPublisherFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FetchByIdAndYearResponse.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualPublisher = response.getBodyObject(FetchByIdAndYearResponse.class);
        assertThat(actualPublisher, is(equalTo(expectedPublisher)));
    }

    @Test
    void shouldReturnChannelIdWithRequestedYearIfThirdPartyDoesNotProvideYear() throws IOException {
        var year = String.valueOf(YEAR_START);
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier);

        var expectedPublisher = mockPublisherFoundYearValueNull(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FetchByIdAndYearResponse.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualPublisher = response.getBodyObject(FetchByIdAndYearResponse.class);
        assertThat(actualPublisher, is(equalTo(expectedPublisher)));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("invalidYearsProvider")
    void shouldReturnBadRequestWhenPathParameterYearIsNotValid(String year)
        throws IOException {

        var input = constructRequest(year, UUID.randomUUID().toString());

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

        var input = constructRequest(String.valueOf(randomYear()), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(containsString("Pid")));
    }

    @Test
    void shouldReturnNotFoundWhenExternalApiRespondsWithNotFound() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = String.valueOf(randomYear());

        mockResponseWithHttpStatus(DATAPORTEN_PATH_ELEMENT, identifier, year, HttpURLConnection.HTTP_NOT_FOUND);

        var input = constructRequest(year, identifier);

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

        mockResponseWithHttpStatus(DATAPORTEN_PATH_ELEMENT, identifier, year, HttpURLConnection.HTTP_INTERNAL_ERROR);

        var input = constructRequest(year, identifier);

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

        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment, publicationChannelClient);

        var input = constructRequest(String.valueOf(randomYear()), UUID.randomUUID().toString());

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Unable to reach upstream!"));
        assertThat(appender.getMessages(), containsString(InterruptedException.class.getSimpleName()));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), is(equalTo("Unable to reach upstream!")));
    }

    private static Stream<String> invalidYearsProvider() {
        var yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", yearAfterNextYear, "21000");
    }

    private FetchByIdAndYearResponse mockPublisherFound(int year, String identifier) {
        var name = randomString();
        var isbnPrefix = String.valueOf(randomIsbnPrefix());
        var scientificValue = randomElement(ScientificValue.values());
        var level = scientificValueToLevel(scientificValue);
        var landingPage = randomUri();
        var body = createDataportenPublisherResponse(year, name, identifier, isbnPrefix, landingPage, level);

        mockDataportenResponse(DATAPORTEN_PATH_ELEMENT, String.valueOf(year), identifier, body);

        return getFetchByIdAndYearResponse(String.valueOf(year), identifier, name, isbnPrefix, scientificValue,
                                           landingPage);
    }

    private FetchByIdAndYearResponse mockPublisherFoundYearValueNull(String year, String identifier) {
        var name = randomString();
        var isbnPrefix = String.valueOf(randomIsbnPrefix());
        var scientificValue = randomElement(ScientificValue.values());
        var level = scientificValueToLevel(scientificValue);
        var landingPage = randomUri();
        var body = createDataportenPublisherResponse(null, name, identifier, isbnPrefix, landingPage, level);

        mockDataportenResponse(DATAPORTEN_PATH_ELEMENT, year, identifier, body);

        return getFetchByIdAndYearResponse(year, identifier, name, isbnPrefix, scientificValue,
                                           landingPage);
    }

    private FetchByIdAndYearResponse getFetchByIdAndYearResponse(
        String year,
        String identifier,
        String name,
        String isbnPrefix,
        ScientificValue scientificValue,
        URI landingPage) {

        var selfUriBase = URI.create(SELF_URI_BASE);
        var publisher = createPublisher(
            year,
            identifier,
            name,
            isbnPrefix,
            scientificValue,
            landingPage);

        return FetchByIdAndYearResponse.create(selfUriBase, publisher, year);
    }
}
