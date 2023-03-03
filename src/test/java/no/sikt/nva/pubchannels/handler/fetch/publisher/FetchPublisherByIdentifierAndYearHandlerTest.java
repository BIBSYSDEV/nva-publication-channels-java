package no.sikt.nva.pubchannels.handler.fetch.publisher;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.fetch.journal.DataportenBodyBuilder;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublicationChannel;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.testutils.RandomDataGenerator;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.SingletonCollector;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;

@WireMockTest(httpsEnabled = true)
class FetchPublisherByIdentifierAndYearHandlerTest {

    public static final int YEAR_START = 1900;
    public static final String SELF_URI_BASE = "https://localhost/publication-channels/publisher";
    private FetchPublisherByIdentifierAndYearHandler handlerUnderTest;
    private ByteArrayOutputStream output;

    private static final Context context = new FakeContext();

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new DataportenPublicationChannelClient(httpClient, dataportenBaseUri, null);
        this.handlerUnderTest = new FetchPublisherByIdentifierAndYearHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(year, identifier);

        var expectedJournal = mockPublisherFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FetchByIdAndYearResponse.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualJournal = response.getBodyObject(FetchByIdAndYearResponse.class);
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
    void shouldReturnNotFoundWhenExternalApiRespondsWithNotFound() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var year = randomYear();

        mockResponseWithHttpStatus(identifier, year, HttpURLConnection.HTTP_NOT_FOUND);

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

        mockResponseWithHttpStatus(identifier, year, HttpURLConnection.HTTP_INTERNAL_ERROR);

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

    private void mockResponseWithHttpStatus(String identifier, String year, int httpStatus) {
        stubFor(
                get("/findpublisher/" + identifier + "/" + year)
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(httpStatus)));
    }

    private FetchByIdAndYearResponse mockPublisherFound(String year, String identifier) {
        var name = randomString();
        var electronicIssn = randomIssn();
        var issn = randomIssn();
        var scientificValue = RandomDataGenerator.randomElement(ScientificValue.values());
        var level = scientificValueToLevel(scientificValue);
        var landingPage = randomUri();
        var type = "Publisher";

        String body = getExpectedResponseBody(year, identifier, name, electronicIssn, issn, level, landingPage, type);

        mockDataportenResponse(year, identifier, body);

        return getFetchByIdAndYearResponse(year, identifier, name, electronicIssn, issn, scientificValue, landingPage);
    }

    private void mockDataportenResponse(String year, String identifier, String responseBody) {
        stubFor(
                get("/findpublisher/" + identifier + "/" + year)
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpURLConnection.HTTP_OK)
                                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                                        .withBody(responseBody)));
    }

    private String getExpectedResponseBody(
            String year,
            String identifier,
            String name,
            String electronicIssn,
            String issn,
            String level,
            URI landingPage,
            String type) {

        return new DataportenBodyBuilder()
                .withType(type)
                .withYear(year)
                .withPid(identifier)
                .withName(name)
                .withEissn(electronicIssn)
                .withPissn(issn)
                .withLevel(level)
                .withKurl(landingPage.toString())
                .build();
    }

    private FetchByIdAndYearResponse getFetchByIdAndYearResponse(
            String year,
            String identifier,
            String name,
            String electronicIssn,
            String issn,
            ScientificValue scientificValue,
            URI landingPage) {

        URI selfUriBase = URI.create(SELF_URI_BASE);
        ThirdPartyPublicationChannel publisher = getPublisher(
                year,
                identifier,
                name,
                electronicIssn,
                issn,
                scientificValue,
                landingPage);

        return FetchByIdAndYearResponse.create(selfUriBase, publisher);
    }

    private ThirdPartyPublicationChannel getPublisher(
            String year,
            String identifier,
            String name,
            String electronicIssn,
            String issn,
            ScientificValue scientificValue,
            URI landingPage) {

        return new ThirdPartyPublicationChannel() {
            @Override
            public String getIdentifier() {
                return identifier;
            }

            @Override
            public String getYear() {
                return year;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getOnlineIssn() {
                return electronicIssn;
            }

            @Override
            public String getPrintIssn() {
                return issn;
            }

            @Override
            public ScientificValue getScientificValue() {
                return scientificValue;
            }

            @Override
            public URI getHomepage() {
                return landingPage;
            }
        };
    }

    private String randomYear() {
        var bound = (LocalDate.now().getYear() + 1) - YEAR_START;
        return Integer.toString(YEAR_START + randomInteger(bound));
    }

    private static InputStream constructRequest(String year, String identifier) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper)
                .withPathParameters(Map.of(
                        "identifier", identifier,
                        "year", year
                ))
                .build();
    }

    private static Stream<String> invalidYearsProvider() {
        String yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", yearAfterNextYear, "21000");
    }

    private String scientificValueToLevel(ScientificValue scientificValue) {

        return ScientificValueMapper.VALUES.entrySet()
                .stream()
                .filter(item -> item.getValue().equals(scientificValue))
                .map(Map.Entry::getKey)
                .collect(SingletonCollector.collectOrElse(null));
    }

}
