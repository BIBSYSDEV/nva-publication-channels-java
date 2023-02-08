package no.sikt.nva.pubchannels.handler;

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
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelSource;
import no.sikt.nva.pubchannels.model.JournalDto;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
public class FetchJournalByIdentifierAndYearHandlerTest {

    private static final int YEAR_START = 1900;
    private static final int RANDOM_YEAR_BOUND = 220;

    private transient FetchJournalByIdentifierAndYearHandler handlerUnderTest;
    private transient PublicationChannelMockRegistry mockRegistry;

    private transient ByteArrayOutputStream output;

    private static final Context context = new FakeContext();
    private transient Environment environment;

    @BeforeEach
    public void setup(WireMockRuntimeInfo runtimeInfo) {

        this.environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelSource = new DataportenPublicationChannelSource(httpClient,
                                                                              dataportenBaseUri);
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource);
        this.mockRegistry = new PublicationChannelMockRegistry();
        this.output = new ByteArrayOutputStream();
    }

    @Test
    public void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = randomYear();
        var identifier = mockRegistry.randomJournal(year);

        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(
                            "identifier", identifier,
                            "year", year
                        ))
                        .build();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, JournalDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualJournal = response.getBodyObject(JournalDto.class);
        var expectedJournal = mockRegistry.getJournal(identifier);
        assertThat(actualJournal, is(equalTo(expectedJournal)));
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @ValueSource(strings = {" ", "abcd", "1899", "3000", "6ba7b810-9dad-11d1-80b4-00c04fd430c8"})
    public void shouldReturnBadRequestWhenPathParameterYearIsNotValid(String year)
        throws IOException {

        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(
                            "identifier", UUID.randomUUID().toString(),
                            "year", year
                        ))
                        .build();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Invalid path parameter (year). Must be an integer between 1900 and 2999.")));
    }

    @ParameterizedTest(name = "identifier \"{0}\" is invalid")
    @ValueSource(strings = { " ", "abcd", "ab78ab78ab78ab78ab78a7ba87b8a7ba87b8" })
    public void shouldReturnBadRequestWhenPathParameterIdentifierIsNotValid(String identifier)
        throws IOException {

        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(
                            "identifier", identifier,
                            "year", randomYear()
                        ))
                        .build();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Invalid path parameter (identifier). Must be a UUID version 4.")));
    }

    private String randomYear() {
        return Integer.toString(YEAR_START + randomInteger(RANDOM_YEAR_BOUND));
    }

    @Test
    public void shouldReturnBadGatewayWhenChannelRegistryIsUnavailable() throws IOException {
        var httpClient = WiremockHttpClient.create();
        var dataportenBaseUri = URI.create("https://localhost:9898");
        var publicationChannelSource = new DataportenPublicationChannelSource(httpClient, dataportenBaseUri);
        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource);

        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(
                            "identifier", identifier,
                            "year", year
                        ))
                        .build();


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
        var publicationChannelSource = new DataportenPublicationChannelSource(httpClient,
                                                                              dataportenBaseUri);

        this.handlerUnderTest = new FetchJournalByIdentifierAndYearHandler(environment, publicationChannelSource);

        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(
                            "identifier", UUID.randomUUID().toString(),
                            "year", randomYear()
                        ))
                        .build();

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Unable to reach upstream!"));
        assertThat(appender.getMessages(), containsString(InterruptedException.class.getSimpleName()));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(),
                   is(equalTo("Unable to reach upstream!")));

    }

    @Test
    public void shouldReturnNotFoundWhenExternalApiRespondsWithNotFound() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        mockRegistry.notFoundJournal(identifier, year);

        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(
                            "identifier", identifier,
                            "year", year
                        ))
                        .build();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_NOT_FOUND)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Journal not found!")));
    }

    @Test
    public void shouldLogAndReturnBadGatewayWhenChannelRegistryReturnsUnhandledResponseCode() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        mockRegistry.internalServerErrorJournal(identifier, year);

        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(
                            "identifier", identifier,
                            "year", year
                        ))
                        .build();

        var appender = LogUtils.getTestingAppenderForRootLogger();
        handlerUnderTest.handleRequest(input, output, context);

        assertThat(appender.getMessages(), containsString("Error fetching journal: 500"));

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                   is(equalTo("Unexpected response from upstream! Got status code 500.")));
    }
}
