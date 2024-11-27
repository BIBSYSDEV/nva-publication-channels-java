package no.sikt.nva.pubchannels.handler.create.series;

import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_PATH;
import static no.sikt.nva.pubchannels.TestConstants.SERIES_TYPE;
import static no.sikt.nva.pubchannels.handler.TestChannel.createEmptyTestChannel;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.currentYear;
import static no.sikt.nva.pubchannels.handler.TestUtils.currentYearAsInteger;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSeriesRequest;
import no.sikt.nva.pubchannels.channelregistry.model.create.CreateChannelResponse;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.create.BaseCreateSerialPublicationHandlerTest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CreateSeriesHandlerTest extends BaseCreateSerialPublicationHandlerTest {

    @BeforeEach
    void setUp() {
        handlerUnderTest = new CreateSeriesHandler(environment, publicationChannelClient);
        baseUri = UriWrapper.fromHost(environment.readEnv("API_DOMAIN"))
                      .addChild(CUSTOM_DOMAIN_BASE_PATH)
                      .addChild(SERIES_PATH)
                      .getUri();
        channelRegistryPathElement = "/createseries/";
    }

    @Test
    void shouldReturnCreatedSeriesWithSuccess() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var request = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, null, null);

        stubPostResponse(expectedPid, request, HttpURLConnection.HTTP_CREATED);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, SERIES_TYPE).withName(VALID_NAME);
        stubFetchOKResponse(testChannel);

        var requestBody = new CreateSeriesRequestBuilder().withName(VALID_NAME).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));

        var actualLocation = URI.create(response.getHeaders().get(HttpHeaders.LOCATION));
        assertThat(actualLocation, is(equalTo(createPublicationChannelUri(expectedPid, SERIES_PATH, currentYear()))));

        var expectedSeries = testChannel.asSerialPublicationDto(baseUri, currentYear());
        assertThat(response.getBodyObject(SerialPublicationDto.class), is(equalTo(expectedSeries)));
    }

    @ParameterizedTest(name = "Should create series for print ISSN \"{0}\"")
    @MethodSource("validIssn")
    void shouldCreateSeriesWithNameAndPrintIssn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();

        var clientRequest = new ChannelRegistryCreateSeriesRequest(VALID_NAME, issn, null, null);
        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, SERIES_TYPE).withName(VALID_NAME);
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSeriesRequestBuilder().withName(VALID_NAME).withPrintIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @ParameterizedTest(name = "Should create series for online ISSN \"{0}\"")
    @MethodSource("validIssn")
    void shouldCreateSeriesWithNameAndOnlineIssn(String issn) throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var clientRequest = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, issn, null);

        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, SERIES_TYPE).withName(VALID_NAME)
                              .withOnlineIssn(issn);
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSeriesRequestBuilder().withName(VALID_NAME).withOnlineIssn(issn).build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    @Test
    void shouldCreateSeriesWithNameAndHomepage() throws IOException {
        var expectedPid = UUID.randomUUID().toString();
        var homepage = "https://a.valid.url.com";
        var clientRequest = new ChannelRegistryCreateSeriesRequest(VALID_NAME, null, null, homepage);

        stubPostResponse(expectedPid, clientRequest, HttpURLConnection.HTTP_CREATED);

        var testChannel = createEmptyTestChannel(currentYearAsInteger(), expectedPid, SERIES_TYPE)
                              .withName(VALID_NAME)
                              .withSameAs(URI.create(homepage));
        stubFetchOKResponse(testChannel);

        var requestBody =
            new CreateSeriesRequestBuilder()
                .withName(VALID_NAME)
                .withHomepage(homepage)
                .build();
        handlerUnderTest.handleRequest(constructRequest(requestBody), output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_CREATED)));
    }

    private static void stubFetchOKResponse(TestChannel testChannel) {
        var channelRegistryResponse = testChannel.asChannelRegistrySeriesBody();
        var requestUrl = "/findseries/" + testChannel.identifier() + "/" + testChannel.year();
        stubGetResponse(HttpURLConnection.HTTP_OK, requestUrl, channelRegistryResponse);
    }

    private static void stubPostResponse(String expectedPid,
                                         ChannelRegistryCreateSeriesRequest request,
                                         int clientResponseHttpCode)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(clientResponseHttpCode, "/createseries/createpid",
                     dtoObjectMapper.writeValueAsString(new CreateChannelResponse(expectedPid)),
                     dtoObjectMapper.writeValueAsString(request));
    }

    private static void setupBadRequestStub(ChannelRegistryCreateSeriesRequest request)
        throws JsonProcessingException {
        stubAuth(HttpURLConnection.HTTP_OK);
        stubResponse(HttpURLConnection.HTTP_BAD_REQUEST, "/createseries/createpid",
                     dtoObjectMapper.writeValueAsString(PROBLEM),
                     dtoObjectMapper.writeValueAsString(request));
    }
}
