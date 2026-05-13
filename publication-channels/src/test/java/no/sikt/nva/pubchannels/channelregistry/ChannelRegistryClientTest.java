package no.sikt.nva.pubchannels.channelregistry;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.UUID.randomUUID;
import static no.sikt.nva.pubchannels.HttpHeaders.ACCEPT;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE_APPLICATION_JSON;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE_APPLICATION_JSON_UTF8;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Year;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryUpdateChannelRequest.Fields;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.model.TokenBodyResponse;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest(httpsEnabled = true)
class ChannelRegistryClientTest {

  private ChannelRegistryClient client;

  @BeforeEach
  void setUp(WireMockRuntimeInfo runtimeInfo) {
    var channelRegistryBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
    var httpClient = WiremockHttpClient.create();
    var dataportenBaseUri = URI.create(runtimeInfo.getHttpsBaseUrl());
    var authClient = new DataportenAuthClient(httpClient, dataportenBaseUri, "", "");
    this.client = new ChannelRegistryClient(httpClient, channelRegistryBaseUri, authClient);
  }

  @Test
  void shouldThrowUnauthorizedWhenAuthorizingFails() {
    var channelIdentifier = randomPublicationChannelIdentifier();
    var request = createRequest(channelIdentifier, "publisher");

    stubFetchChannelResponse(
        channelIdentifier, HTTP_OK, channelWithScientValue(null), "findpublisher");
    stubTokenResponse(HTTP_UNAUTHORIZED);

    assertThrows(UnauthorizedException.class, () -> client.updateChannel(request));
  }

  private static String randomPublicationChannelIdentifier() {
    return randomUUID().toString().toUpperCase();
  }

  @Test
  void shouldThrowBadRequestWhenAttemptingToUpdateChannelWithUnsupportedType() {
    var channelIdentifier = randomPublicationChannelIdentifier();
    var request = createRequest(channelIdentifier, randomString());

    stubTokenResponse(HTTP_OK);

    assertThrows(BadRequestException.class, () -> client.updateChannel(request));
  }

  @Test
  void shouldThrowNotFoundWhenUpdatingPublisherRespondsWith404WhenFetchingChannel() {
    var channelIdentifier = randomPublicationChannelIdentifier();
    var request = createRequest(channelIdentifier, "publisher");

    stubTokenResponse(HTTP_OK);
    stubFetchChannelResponse(channelIdentifier, HTTP_NOT_FOUND, EMPTY_STRING, "findpublisher");

    assertThrows(NotFoundException.class, () -> client.updateChannel(request));
  }

  @Test
  void shouldThrowNotFoundWhenUpdatingSerialPublicationRespondsWith404WhenFetchingChannel() {
    var channelIdentifier = randomPublicationChannelIdentifier();
    var request = createRequest(channelIdentifier, "serial-publication");

    stubTokenResponse(HTTP_OK);
    stubFetchChannelResponse(channelIdentifier, HTTP_NOT_FOUND, EMPTY_STRING, "findjournalserie");

    assertThrows(NotFoundException.class, () -> client.updateChannel(request));
  }

  @Test
  void shouldThrowBadRequestWhenUpdatingNotUnassignedChannel() {
    var channelIdentifier = randomPublicationChannelIdentifier();
    var request = createRequest(channelIdentifier, "publisher");

    stubTokenResponse(HTTP_OK);
    stubFetchChannelResponse(
        channelIdentifier, HTTP_OK, channelWithScientValue("1"), "findpublisher");

    assertThrows(BadRequestException.class, () -> client.updateChannel(request));
  }

  @Test
  void shouldThrowBadRequestWhenChannelRegistryRespondsWith4XXStatusCodeOnUpdate() {
    var channelIdentifier = randomPublicationChannelIdentifier();
    var request = createRequest(channelIdentifier, "publisher");

    stubTokenResponse(HTTP_OK);
    stubFetchChannelResponse(
        channelIdentifier, HTTP_OK, channelWithScientValue(null), "findpublisher");
    stubUpdateChannelResponse(HTTP_BAD_REQUEST);

    assertThrows(BadRequestException.class, () -> client.updateChannel(request));
  }

  @Test
  void shouldThrowBadGatewayWhenChannelRegistryRespondsWith5XXOnUpdate() {
    var channelIdentifier = randomPublicationChannelIdentifier();
    var request = createRequest(channelIdentifier, "publisher");

    stubTokenResponse(HTTP_OK);
    stubFetchChannelResponse(
        channelIdentifier, HTTP_OK, channelWithScientValue(null), "findpublisher");
    stubUpdateChannelResponse(HTTP_BAD_GATEWAY);

    assertThrows(BadGatewayException.class, () -> client.updateChannel(request));
  }

  @Test
  void shouldThrowBadGatewayWithTimeoutMessageWhenHttpRequestTimesOut() throws Exception {
    var mockHttpClient = mock(HttpClient.class);
    when(mockHttpClient.send(any(), any())).thenThrow(new HttpTimeoutException("timed out"));
    var timeoutClient = new ChannelRegistryClient(mockHttpClient, randomUri(), null);

    var exception =
        assertThrows(
            BadGatewayException.class,
            () -> timeoutClient.searchChannel(ChannelType.JOURNAL, Map.of("name", "test")));

    assertTrue(exception.getMessage().contains("Request to upstream timed out"));
  }

  private static ChannelRegistryUpdateChannelRequest createRequest(
      String channelIdentifier, String type) {
    return new ChannelRegistryUpdateChannelRequest(
        new Fields(channelIdentifier, null, null, null, null), type);
  }

  private static ChannelRegistryLevel randomLevel(String level) {
    return new ChannelRegistryLevel(
        Integer.parseInt(Year.now().toString()), level, level, randomString(), null);
  }

  private static void stubFetchChannelResponse(
      String channelIdentifier, int statusCode, String body, String path) {
    stubFor(
        get(urlPathEqualTo(("/%s/%s/%s").formatted(path, channelIdentifier, Year.now().toString())))
            .withHeader(ACCEPT, WireMock.equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .willReturn(
                aResponse()
                    .withStatus(statusCode)
                    .withHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_UTF8)
                    .withBody(body)));
  }

  private static void stubTokenResponse(int statusCode) {
    stubFor(
        post("/oauth/token")
            .withBasicAuth(EMPTY_STRING, EMPTY_STRING)
            .withHeader(
                HttpHeaders.CONTENT_TYPE,
                WireMock.equalTo(HttpHeaders.CONTENT_TYPE_X_WWW_FORM_URLENCODED))
            .willReturn(
                aResponse()
                    .withStatus(statusCode)
                    .withBody(
                        attempt(
                                () ->
                                    dtoObjectMapper.writeValueAsString(
                                        new TokenBodyResponse("token1", "Bearer")))
                            .orElseThrow())));
  }

  private String channelWithScientValue(String level) {
    return attempt(
            () ->
                JsonUtils.dtoObjectMapper.writeValueAsString(
                    new ChannelRegistryPublisher(
                        randomPublicationChannelIdentifier().toString(),
                        randomLevel(level),
                        null,
                        null,
                        null,
                        null,
                        "publisher")))
        .orElseThrow();
  }

  private void stubUpdateChannelResponse(int statusCode) {
    stubFor(
        patch(urlPathEqualTo("/admin/change"))
            .withHeader(ACCEPT, WireMock.equalTo(CONTENT_TYPE_APPLICATION_JSON))
            .willReturn(
                aResponse()
                    .withStatus(statusCode)
                    .withHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_UTF8)
                    .withBody(EMPTY_STRING)));
  }
}
