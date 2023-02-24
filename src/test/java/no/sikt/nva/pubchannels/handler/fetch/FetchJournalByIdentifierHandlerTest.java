package no.sikt.nva.pubchannels.handler.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.sikt.nva.pubchannels.dataporten.model.DataportenLevel;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.model.DataportenBodyBuilder;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.testutils.RandomDataGenerator;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.SingletonCollector;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomLocalDate;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;

@WireMockTest(httpsEnabled = true)
class FetchJournalByIdentifierHandlerTest {

    public static final String API_DOMAIN = "localhost";
    public static final String CUSTOM_DOMAIN_PATH = "publication-channels";
    public static final String JOURNAL_PATH_ELEMENT = "journal";
    private FetchJournalByIdentifierHandler handlerUnderTest;
    private ByteArrayOutputStream output;
    private static final Context context = new FakeContext();

    @BeforeEach
    void setup(WireMockRuntimeInfo runtimeInfo) {

        var environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
        var httpClient = WiremockHttpClient.create();
        var publicationChannelClient = new DataportenPublicationChannelClient(httpClient, dataportenBaseUri, null);
        this.handlerUnderTest = new FetchJournalByIdentifierHandler(environment, publicationChannelClient);
        this.output = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        output.flush();
    }

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(identifier);

        var expectedJournal = getExpectedJournal(identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FetchJournalByIdentifierResponse.class);

        var statusCode = response.getStatusCode();

        assertThat(statusCode, is(equalTo(HttpURLConnection.HTTP_OK)));

        var actualJournal = response.getBodyObject(FetchJournalByIdentifierResponse.class);

        assertThat(actualJournal, is(equalTo(expectedJournal)));
    }

    @ParameterizedTest(name = "identifier \"{0}\" is invalid")
    @ValueSource(strings = {" ", "abcd", "ab78ab78ab78ab78ab78a7ba87b8a7ba87b8"})
    void shouldReturnBadRequestWhenPathParameterIdentifierIsNotValid(String identifier)
            throws IOException {

        var input = constructRequest(identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(containsString("Pid")));
    }

    private FetchJournalByIdentifierResponse getExpectedJournal(String identifier) {
        var name = randomString();
        var electronicIssn = randomIssn();
        var printIssn = randomIssn();
        var scientificValue = RandomDataGenerator.randomElement(ScientificValue.values());
        var landingPage = randomUri();
        var year = String.valueOf(randomLocalDate().getYear());
        var expectedURI = getUri(identifier);

        stubFetchJournal(identifier,
                name,
                electronicIssn,
                printIssn,
                year,
                scientificValue,
                landingPage,
                HttpURLConnection.HTTP_OK);

        return getExpectedJournal(name,
                electronicIssn,
                printIssn,
                scientificValue,
                landingPage,
                expectedURI);
    }

    private FetchJournalByIdentifierResponse getExpectedJournal(String name,
                                                                String electronicIssn,
                                                                String printIssn,
                                                                ScientificValue scientificValue,
                                                                URI landingPage,
                                                                URI expectedURI) {
        return new FetchJournalByIdentifierResponse(
                expectedURI,
                name,
                electronicIssn,
                printIssn,
                scientificValue,
                landingPage);
    }

    private URI getUri(String identifier) {
        return new UriWrapper(HTTPS, API_DOMAIN)
                .addChild(CUSTOM_DOMAIN_PATH, JOURNAL_PATH_ELEMENT, identifier)
                .getUri();
    }

    private void stubFetchJournal(String identifier, String name,
                                  String eissn, String pissn,
                                  String year,
                                  ScientificValue scientificValue,
                                  URI landingPage, int status) {
        var level = scientificValueToLevel(scientificValue);
        var currentLevel = new DataportenLevel(year, level);

        var body = new DataportenBodyBuilder()
                .withName(name)
                .withEissn(eissn)
                .withPid(identifier)
                .withPissn(pissn)
                .withCurrent(currentLevel)
                .withLevels(List.of(currentLevel))
                .withKurl(landingPage.toString())
                .build();

        stubFor(
                get("/findjournal/" + identifier)
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(status)
                                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                                        .withBody(body)));
    }

    private static InputStream constructRequest(String identifier) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper)
                .withPathParameters(Map.of(
                        "identifier", identifier
                ))
                .build();
    }

    private String scientificValueToLevel(ScientificValue scientificValue) {

        return ScientificValueMapper.VALUES.entrySet()
                .stream()
                .filter(item -> item.getValue().equals(scientificValue))
                .map(Map.Entry::getKey)
                .collect(SingletonCollector.collectOrElse(null));
    }
}
