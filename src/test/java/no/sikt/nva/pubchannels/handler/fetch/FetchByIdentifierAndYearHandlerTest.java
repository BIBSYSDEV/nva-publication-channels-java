package no.sikt.nva.pubchannels.handler.fetch;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static no.sikt.nva.pubchannels.TestConstants.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.sikt.nva.pubchannels.TestConstants.API_DOMAIN;
import static no.sikt.nva.pubchannels.TestConstants.CUSTOM_DOMAIN_BASE_PATH;
import static no.sikt.nva.pubchannels.TestConstants.LOCATION;
import static no.sikt.nva.pubchannels.TestConstants.WILD_CARD;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.createPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockRedirectedClient;
import static no.sikt.nva.pubchannels.handler.TestUtils.mockResponseWithHttpStatus;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.sikt.nva.pubchannels.handler.TestUtils.setupInterruptedClient;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheServiceTestSetup;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

/**
 * Common behaviour for FetchSerialPublicationByIdentifierAndYearHandler,
 * FetchPublisherByIdentifierAndYearHandler, FetchSeriesByIdentifierAndYearHandler and
 * FetchJournalByIdentifierAndYearHandler are tested here.
 */
@WireMockTest(httpsEnabled = true)
public abstract class FetchByIdentifierAndYearHandlerTest extends CacheServiceTestSetup {

  protected static final Context context = new FakeContext();
  protected static Environment environment;
  protected CacheService cacheService;
  protected ByteArrayOutputStream output;
  protected String channelRegistryBaseUri;
  protected String customChannelPath;
  protected String channelRegistryPathElement;
  protected ChannelRegistryClient channelRegistryClient;
  protected FetchByIdentifierAndYearHandler<Void, ?> handlerUnderTest;
  protected static String year = randomYear();
  protected static String name = randomString();
  protected static String identifier = UUID.randomUUID().toString();

  @BeforeAll
  public static void commonBeforeAll() {
    environment = Mockito.mock(Environment.class);
    when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn(WILD_CARD);
    when(environment.readEnv("API_DOMAIN")).thenReturn(API_DOMAIN);
    when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn(CUSTOM_DOMAIN_BASE_PATH);
  }

  protected abstract FetchByIdentifierAndYearHandler<Void, ?> createHandler(
      ChannelRegistryClient publicationChannelClient);

  @BeforeEach
  void commonBeforeEach(WireMockRuntimeInfo runtimeInfo) {
    super.setupDynamoDbTable();
    channelRegistryBaseUri = runtimeInfo.getHttpsBaseUrl();
    HttpClient httpClient = WiremockHttpClient.create();
    channelRegistryClient =
        new ChannelRegistryClient(httpClient, URI.create(channelRegistryBaseUri), null);
    cacheService = new CacheService(super.getClient());
    output = new ByteArrayOutputStream();
  }

  @ParameterizedTest(name = "year {0} is invalid")
  @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#invalidYearsProvider")
  void shouldReturnBadRequestWhenPathParameterYearIsNotValid(String invalidYear)
      throws IOException {

    var input = constructRequest(invalidYear, identifier, MediaType.ANY_TYPE);

    handlerUnderTest.handleRequest(input, output, context);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_REQUEST)));

    var problem = response.getBodyObject(Problem.class);

    assertThat(problem.getDetail(), is(containsString("Year")));
  }

  @ParameterizedTest(name = "identifier \"{0}\" is invalid")
  @ValueSource(strings = {" ", "abcd", "ab78ab78ab78ab78ab78a7ba87b8a7ba87b8"})
  void shouldReturnBadRequestWhenPathParameterIdentifierIsNotValid(String invalidIdentifier)
      throws IOException {

    var input = constructRequest(year, invalidIdentifier, MediaType.ANY_TYPE);

    handlerUnderTest.handleRequest(input, output, context);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_REQUEST)));

    var problem = response.getBodyObject(Problem.class);

    assertThat(problem.getDetail(), is(containsString("Pid")));
  }

  @Test
  void shouldReturnNotFoundWhenExternalApiRespondsWithNotFound() throws IOException {
    mockResponseWithHttpStatus(channelRegistryPathElement, identifier, year, HTTP_NOT_FOUND);

    var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

    handlerUnderTest.handleRequest(input, output, context);

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertThat(response.getStatusCode(), is(equalTo(HTTP_NOT_FOUND)));

    var problem = response.getBodyObject(Problem.class);

    assertThat(problem.getDetail(), is(equalTo("Publication channel not found!")));
  }

  @Test
  void
      shouldLogAndReturnBadGatewayWhenChannelClientReturnsUnhandledResponseCodeAndChannelIsNotCached()
          throws IOException {
    mockResponseWithHttpStatus(channelRegistryPathElement, identifier, year, HTTP_INTERNAL_ERROR);

    var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

    var appender = LogUtils.getTestingAppenderForRootLogger();
    handlerUnderTest.handleRequest(input, output, context);

    assertThat(appender.getMessages(), containsString("Error fetching publication channel"));
    assertThat(appender.getMessages(), containsString("500"));

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_GATEWAY)));

    var problem = response.getBodyObject(Problem.class);

    assertThat(problem.getDetail(), is(equalTo("Unexpected response from upstream!")));
  }

  @Test
  void shouldLogErrorAndReturnBadGatewayWhenInterruptionOccurs()
      throws IOException, InterruptedException {
    var publicationChannelClient = setupInterruptedClient();

    this.handlerUnderTest = createHandler(publicationChannelClient);

    var input = constructRequest(year, identifier, MediaType.ANY_TYPE);

    var appender = LogUtils.getTestingAppenderForRootLogger();
    handlerUnderTest.handleRequest(input, output, context);

    assertThat(appender.getMessages(), containsString("Unable to reach upstream!"));
    assertThat(appender.getMessages(), containsString(InterruptedException.class.getSimpleName()));

    var response = GatewayResponse.fromOutputStream(output, Problem.class);

    assertThat(response.getStatusCode(), is(equalTo(HTTP_BAD_GATEWAY)));

    var problem = response.getBodyObject(Problem.class);
    assertThat(problem.getDetail(), is(equalTo("Unable to reach upstream!")));
  }

  @Test
  void shouldReturnRedirectWhenChannelRegistryReturnsRedirect() throws IOException {
    var requestedIdentifier = UUID.randomUUID().toString();
    var newIdentifier = UUID.randomUUID().toString();
    var newChannelRegistryLocation =
        UriWrapper.fromHost(channelRegistryBaseUri)
            .addChild(channelRegistryPathElement, newIdentifier, year)
            .toString();
    mockRedirectedClient(
        requestedIdentifier, newChannelRegistryLocation, year, channelRegistryPathElement);
    handlerUnderTest.handleRequest(
        constructRequest(year, requestedIdentifier, MediaType.ANY_TYPE), output, context);
    var response = GatewayResponse.fromOutputStream(output, HttpResponse.class);
    assertEquals(HTTP_MOVED_PERM, response.getStatusCode());
    var expectedLocation =
        createPublicationChannelUri(newIdentifier, customChannelPath, year).toString();
    assertEquals(expectedLocation, response.getHeaders().get(LOCATION));
    assertEquals(WILD_CARD, response.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
  }
}
