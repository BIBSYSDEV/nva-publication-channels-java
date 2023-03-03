package no.sikt.nva.pubchannels.handler.fetch.journal;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
class FetchJournalByIdentifierAndYearHandlerTest {

    private static final int YEAR_START = 2004;

    private FetchJournalByIdentifierAndYearHandler handlerUnderTest;
    private PublicationChannelMockClient mockRegistry;

    private ByteArrayOutputStream output;

    private static final Context context = new FakeContext();
    private Environment environment;

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {

        this.environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelSource = new DataportenPublicationChannelClient(httpClient, dataportenBaseUri, null);
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource);
        this.mockRegistry = new PublicationChannelMockClient();
        this.output = new ByteArrayOutputStream();
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = randomYear();
        var identifier = mockRegistry.randomJournal(year);
        var input = constructRequest(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FetchByIdAndYearResponse.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualJournal = response.getBodyObject(FetchByIdAndYearResponse.class);
        var expectedJournal = mockRegistry.getJournal(identifier);
        assertThat(actualJournal, is(equalTo(expectedJournal)));
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

        var input = constructRequest(randomYear(), identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(containsString("Pid")));
    }

    @Test
    void shouldReturnBadGatewayWhenChannelRegistryIsUnavailable() throws IOException {
        var httpClient = WiremockHttpClient.create();
        var dataportenBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new DataportenPublicationChannelClient(httpClient, dataportenBaseUri, null);
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

        assertThat(problem.getDetail(),
                   is(equalTo("Unable to reach upstream!")));
    }

    @Test
    void shouldLogErrorAndReturnBadGatewayWhenInterruptionOccurs() throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        when(httpClient.send(any(), any())).thenThrow(new InterruptedException());
        var dataportenBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new DataportenPublicationChannelClient(httpClient,
                                                                              dataportenBaseUri, null);

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

        assertThat(appender.getMessages(), containsString("Error fetching publication channel: 500"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream!")));
    }

    private static InputStream constructRequest(String year, String identifier) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper)
                   .withPathParameters(Map.of(
                       "identifier", identifier,
                       "year", year
                   ))
                   .build();
    }

    private String randomYear() {
        var bound = (LocalDate.now().getYear() + 1) - YEAR_START;
        return Integer.toString(YEAR_START + randomInteger(bound));
    }

    private static Stream<String> invalidYearsProvider() {
        String yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", "2003", yearAfterNextYear, "21000");
    }
}
