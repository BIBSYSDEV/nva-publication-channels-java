package no.sikt.nva.pubchannels.handler.search.serialpublication;

import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_OFFSET_INT;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_SIZE_INT;
import static no.sikt.nva.pubchannels.TestConstants.JOURNAL_TYPE;
import static no.sikt.nva.pubchannels.TestConstants.SERIAL_PUBLICATION_PATH;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
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
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.handler.search.SearchByQueryHandlerTest;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import no.unit.nva.stubs.FakeContext;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.UnprocessableContentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SearchSerialPublicationByQueryHandlerTest extends SearchByQueryHandlerTest {

    private static final Context context = new FakeContext();
    private static final URI SELF_URI_BASE = URI.create(
        "https://localhost/publication-channels/" + SERIAL_PUBLICATION_PATH);
    private static final TypeReference<PaginatedSearchResult<SerialPublicationDto>> TYPE_REF = new TypeReference<>() {
    };

    @Override
    protected String getPath() {
        return ChannelType.SERIAL_PUBLICATION.pathElement;
    }

    @BeforeEach
    void setup() {
        this.handlerUnderTest = new SearchSerialPublicationByQueryHandler(environment, publicationChannelClient);
    }

    @ParameterizedTest(name = "Should return requested media type \"{0}\"")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType)
        throws IOException, UnprocessableContentException {
        var year = randomYear();
        var issn = randomIssn();
        var testChannel = new TestChannel(year, UUID.randomUUID().toString(), JOURNAL_TYPE).withPrintIssn(issn);
        mockChannelRegistryResponse(year, issn, List.of(testChannel.asChannelRegistrySerialPublicationBody()));
        var input = constructRequest(Map.of("year", year, "query", issn), mediaType);

        this.handlerUnderTest.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
        var pagesSearchResult = objectMapper.readValue(response.getBody(), TYPE_REF);
        var contentType = response.getHeaders().get(CONTENT_TYPE);

        var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedSearchResult = getExpectedSearchResult(String.valueOf(year), issn, testChannel);
        assertThat(pagesSearchResult.getHits(), containsInAnyOrder(expectedSearchResult.getHits().toArray()));
        assertThat(contentType, is(equalTo(expectedMediaType)));
        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
    }

    private static PaginatedSearchResult<SerialPublicationDto> getExpectedSearchResult(String year,
                                                                                       String printIssn,
                                                                                       TestChannel testChannel)
        throws UnprocessableContentException {
        var expectedParams = new HashMap<String, String>();
        expectedParams.put("query", printIssn);
        if (nonNull(year)) {
            expectedParams.put("year", year);
        }

        var expectedHits = List.of(testChannel.asSerialPublicationDto(SELF_URI_BASE, year));

        return PaginatedSearchResult.create(constructPublicationChannelUri(testChannel.type(), expectedParams),
                                            DEFAULT_OFFSET_INT,
                                            DEFAULT_SIZE_INT,
                                            expectedHits.size(),
                                            expectedHits);
    }
}
