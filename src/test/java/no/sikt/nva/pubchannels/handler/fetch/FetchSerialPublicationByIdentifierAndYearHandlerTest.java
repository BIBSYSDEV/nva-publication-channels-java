package no.sikt.nva.pubchannels.handler.fetch;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.sikt.nva.pubchannels.HttpHeaders.CONTENT_TYPE;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.sikt.nva.pubchannels.handler.TestUtils.randomYear;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.util.UUID;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.apigateway.GatewayResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

// Common behavior for FetchSeriesByIdentifierAndYearHandler, FetchSerialPublicationByIdentifierAndYearHandler,
// and FetchJournalByIdentifierAndYearHandler is tested here
public abstract class FetchSerialPublicationByIdentifierAndYearHandlerTest extends FetchByIdentifierAndYearHandlerTest {

    protected abstract SerialPublicationDto mockChannelFound(int year, String identifier);

    protected abstract SerialPublicationDto mockChannelFoundYearValueNull(int year, String identifier);

    protected abstract SerialPublicationDto mockChannelWithScientificValueReviewNotice(int year, String identifier);

    @Test
    void shouldReturnCorrectDataWithSuccessWhenExists() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);

        var expectedSeries = mockChannelFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
    }

    @Test
    void shouldIncludeYearInResponse() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);
        mockChannelFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualYear = response.getBodyObject(SerialPublicationDto.class).year();
        assertThat(actualYear, is(equalTo(String.valueOf(year))));
    }

    @ParameterizedTest(name = "Should return requested media type \"{0}\"")
    @MethodSource("no.sikt.nva.pubchannels.handler.TestUtils#mediaTypeProvider")
    void shouldReturnContentNegotiatedContentWhenRequested(MediaType mediaType) throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, mediaType);

        final var expectedMediaType =
            mediaType.equals(MediaType.ANY_TYPE) ? MediaType.JSON_UTF_8.toString() : mediaType.toString();
        var expectedSeries = mockChannelFound(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));
        var contentType = response.getHeaders().get(CONTENT_TYPE);
        assertThat(contentType, is(equalTo(expectedMediaType)));
    }

    @Test
    void shouldReturnChannelIdWithRequestedYearIfThirdPartyDoesNotProvideYear() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();

        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);

        var expectedSeries = mockChannelFoundYearValueNull(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);

        var statusCode = response.getStatusCode();
        assertThat(statusCode, is(equalTo(HTTP_OK)));

        var actualSeries = response.getBodyObject(SerialPublicationDto.class);
        assertThat(actualSeries, is(equalTo(expectedSeries)));
    }

    @Test
    void shouldIncludeScientificReviewNoticeWhenLevelDisplayX() throws IOException {
        var year = randomYear();
        var identifier = UUID.randomUUID().toString();
        var input = constructRequest(String.valueOf(year), identifier, MediaType.ANY_TYPE);
        var expectedSeries = mockChannelWithScientificValueReviewNotice(year, identifier);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, SerialPublicationDto.class);
        var actualReviewNotice = response.getBodyObject(SerialPublicationDto.class).reviewNotice();
        assertThat(actualReviewNotice, is(equalTo(expectedSeries.reviewNotice())));
    }
}
