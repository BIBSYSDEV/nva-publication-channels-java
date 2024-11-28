package no.sikt.nva.pubchannels.handler.search;

import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_OFFSET_INT;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_SIZE_INT;
import static no.sikt.nva.pubchannels.TestConstants.ISSN_QUERY_PARAM;
import static no.sikt.nva.pubchannels.TestConstants.NAME_QUERY_PARAM;
import static no.sikt.nva.pubchannels.handler.TestUtils.areEqualURIs;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import no.unit.nva.stubs.FakeContext;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.UnprocessableContentException;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public abstract class BaseSearchSerialPublicationByQueryHandlerTest extends SearchByQueryHandlerTest {

    private static final Context context = new FakeContext();
    private static final TypeReference<PaginatedSearchResult<SerialPublicationDto>> TYPE_REF = new TypeReference<>() {
    };
    protected String type;
    protected URI selfBaseUri;

    // Test data
    protected static String year = randomYear();
    protected static String issn = randomIssn();
    protected static String name = randomString();

    @ParameterizedTest(name = "Should return requested media type \"{0}\"")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType)
        throws IOException, UnprocessableContentException {
        var testChannel = new TestChannel(year, UUID.randomUUID().toString(), type).withPrintIssn(issn);
        mockChannelRegistryResponse(year,
                                    ISSN_QUERY_PARAM,
                                    issn,
                                    List.of(testChannel.asChannelRegistrySerialPublicationBody()));

        var expectedSearchResult = getExpectedSearchResult(year, issn, testChannel);
        var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();

        var input = constructRequest(Map.of("year", year, "query", issn), mediaType);
        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);
        var contentType = response.getHeaders().get(CONTENT_TYPE);

        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
        assertThat(contentType, is(equalTo(expectedMediaType)));
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsIssn() throws IOException, UnprocessableContentException {
        var expectedSearchResult = getExpectedPaginatedSearchResultIssnSearch(year, issn);

        var input = constructRequest(Map.of("year", year, "query", issn), MediaType.ANY_TYPE);
        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithRequestedYearIfThirdPartyDoesNotProvideYear()
        throws IOException, UnprocessableContentException {
        var expectedSearchResult = getExpectedPaginatedSearchResultIssnSearchThirdPartyDoesNotProvideYear(year, issn);

        var input = constructRequest(Map.of("year", year, "query", issn), MediaType.ANY_TYPE);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithSuccessWhenQueryIsName() throws IOException, UnprocessableContentException {
        var expectedSearchResult = getExpectedPaginatedSearchResultNameSearch(year, name);

        var input = constructRequest(Map.of("year", year, "query", name), MediaType.ANY_TYPE);
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(pagesSearchResult.getTotalHits(), is(equalTo(expectedSearchResult.getHits().size())));
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
    }

    @Test
    void shouldReturnResultWithYearWhenQueryOmitsYear() throws IOException {
        var testChannel = new TestChannel(year, UUID.randomUUID().toString(), type).withName(name);
        mockChannelRegistryResponse(null,
                                    NAME_QUERY_PARAM,
                                    name,
                                    List.of(testChannel.asChannelRegistrySerialPublicationBody()));

        var input = constructRequest(Map.of("query", name), MediaType.ANY_TYPE);
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var result = objectMapper.readValue(response.getBody(), TYPE_REF).getHits().getFirst();

        assertThat(result.year(), is(equalTo(year)));
    }

    @Test
    void shouldReturnResultWithIdMatchingRequest() throws IOException {
        var size = 20;
        var offset = 40;
        var testChannel = new TestChannel(year, UUID.randomUUID().toString(), type).withPrintIssn(issn);
        var expectedId = UriWrapper.fromUri(selfBaseUri)
                                   .addQueryParameter("year", year)
                                   .addQueryParameter("query", issn)
                                   .addQueryParameter("size", String.valueOf(size))
                                   .addQueryParameter("offset", String.valueOf(offset))
                                   .getUri();
        mockChannelRegistryResponse(year,
                                    ISSN_QUERY_PARAM,
                                    testChannel.getPrintIssnValue(),
                                    List.of(testChannel.asChannelRegistrySerialPublicationBody()),
                                    size,
                                    offset);

        var input = constructRequest(Map.of("year",
                                            year,
                                            "query",
                                            issn,
                                            "size",
                                            String.valueOf(size),
                                            "offset",
                                            String.valueOf(offset)), MediaType.ANY_TYPE);
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualId = objectMapper.readValue(response.getBody(), TYPE_REF).getId();

        assertTrue(areEqualURIs(actualId, expectedId));
    }

    @ParameterizedTest(name = "Should accept offset \"{0}\"")
    @ValueSource(ints = {0, 10, 20})
    void shouldReturnIdWithOffsetMatchingRequest(int offset) throws IOException {
        var testChannel = new TestChannel(year, UUID.randomUUID().toString(), type).withPrintIssn(issn);
        mockChannelRegistryResponse(year,
                                    ISSN_QUERY_PARAM,
                                    testChannel.getPrintIssnValue(),
                                    List.of(testChannel.asChannelRegistrySerialPublicationBody()),
                                    DEFAULT_SIZE_INT,
                                    offset);

        var input = constructRequest(Map.of("year", year, "query", issn, "offset", String.valueOf(offset)),
                                     MediaType.ANY_TYPE);
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualId = objectMapper.readValue(response.getBody(), TYPE_REF).getId();

        assertThat(actualId.toString(), containsStringIgnoringCase("offset=" + offset));
    }

    @ParameterizedTest(name = "Should accept size \"{0}\"")
    @ValueSource(ints = {1, 10, 20})
    void shouldReturnIdWithSizeMatchingRequest(int size) throws IOException {
        var testChannel = new TestChannel(year, UUID.randomUUID().toString(), type).withPrintIssn(issn);
        mockChannelRegistryResponse(year,
                                    ISSN_QUERY_PARAM,
                                    testChannel.getPrintIssnValue(),
                                    List.of(testChannel.asChannelRegistrySerialPublicationBody()),
                                    size,
                                    DEFAULT_OFFSET_INT);

        var input = constructRequest(Map.of("year", year, "query", issn, "size", String.valueOf(size)),
                                     MediaType.ANY_TYPE);
        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var actualId = objectMapper.readValue(response.getBody(), TYPE_REF).getId();

        assertThat(actualId.toString(), containsStringIgnoringCase("size=" + size));
    }

    private PaginatedSearchResult<SerialPublicationDto> getExpectedPaginatedSearchResultNameSearch(String year,
                                                                                                   String name)
        throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, pid, type).withName(name);
        mockChannelRegistryResponse(year,
                                    NAME_QUERY_PARAM,
                                    name,
                                    List.of(testChannel.asChannelRegistrySerialPublicationBody()));

        return getExpectedSearchResult(year, name, testChannel);
    }

    private PaginatedSearchResult<SerialPublicationDto> getExpectedPaginatedSearchResultIssnSearch(
        String year, String printIssn)
        throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(year, pid, type).withPrintIssn(printIssn);
        mockChannelRegistryResponse(year,
                                    ISSN_QUERY_PARAM,
                                    printIssn,
                                    List.of(testChannel.asChannelRegistrySerialPublicationBody()));

        return getExpectedSearchResult(year, printIssn, testChannel);
    }

    private PaginatedSearchResult<SerialPublicationDto> getExpectedPaginatedSearchResultIssnSearchThirdPartyDoesNotProvideYear(
        String year, String printIssn)
        throws UnprocessableContentException {
        var pid = UUID.randomUUID().toString();
        var testChannel = new TestChannel(null, pid, type).withPrintIssn(printIssn);
        mockChannelRegistryResponse(year,
                                    ISSN_QUERY_PARAM,
                                    printIssn,
                                    List.of(testChannel.asChannelRegistrySerialPublicationBody()));

        return getExpectedSearchResult(year, printIssn, testChannel);
    }

    private PaginatedSearchResult<SerialPublicationDto> getExpectedSearchResult(
        String year,
        String queryParamValue,
        TestChannel testChannel) throws UnprocessableContentException {
        var expectedParams = new HashMap<String, String>();
        expectedParams.put("query", queryParamValue);
        if (nonNull(year)) {
            expectedParams.put("year", year);
        }

        var expectedHits = List.of(testChannel.asSerialPublicationDto(selfBaseUri, year));
        return PaginatedSearchResult.create(constructPublicationChannelUri(testChannel.type(), expectedParams),
                                            DEFAULT_OFFSET_INT,
                                            DEFAULT_SIZE_INT,
                                            expectedHits.size(),
                                            expectedHits);
    }
}

