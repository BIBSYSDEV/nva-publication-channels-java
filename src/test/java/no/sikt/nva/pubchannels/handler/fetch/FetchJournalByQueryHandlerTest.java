package no.sikt.nva.pubchannels.handler.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
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
import java.time.LocalDate;
import java.time.Year;
import java.util.Map;
import java.util.stream.Stream;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;

@WireMockTest(httpsEnabled = true)
class FetchJournalByQueryHandlerTest {

    private FetchJournalByQueryHandler handlerUnderTest;
    private ByteArrayOutputStream output;
    private static final Context context = new FakeContext();

    @BeforeEach
    void setup() {

        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv("ALLOWED_ORIGIN")).thenReturn("*");
        when(environment.readEnv("API_DOMAIN")).thenReturn("localhost");
        when(environment.readEnv("CUSTOM_DOMAIN_BASE_PATH")).thenReturn("publication-channels");

        this.handlerUnderTest = new FetchJournalByQueryHandler(environment);
        this.output = new ByteArrayOutputStream();
    }

    @ParameterizedTest(name = "year {0} is invalid")
    @MethodSource("invalidYearsProvider")
    void shouldReturnBadRequestWhenYearIsInvalid(String year) throws IOException {
        var queryParameters = Map.of("year", year);
        var input = constructRequest(queryParameters);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(containsString("Year")));
    }

    @Test
    void shouldReturnBadRequestWhenMissingQueryParamYear() throws IOException {
        var input = constructRequest(null);

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(), is(containsString("year")));
    }

    @Test
    void shouldReturnBadRequestWhenQueryParamTooLong() throws IOException {
        var input = constructRequest(Map.of("year", randomValidYear(),
                "query", "Lorem Ipsum is simply dummy text of the " +
                        "printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text" +
                        " ever since the 1500s, when an unknown printer took a galley of type and scrambled it to " +
                        "make a type specimen book. It has survived not only five centuries, but also the l"));

        this.handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));

        var problem = response.getBodyObject(Problem.class);

        assertThat(problem.getDetail(),
                is(containsString("Query")));
    }

    private static InputStream constructRequest(Map<String, String> queryParameters) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(dtoObjectMapper)
                .withQueryParameters(queryParameters)
                .build();
    }

    private static Stream<String> invalidYearsProvider() {
        String yearAfterNextYear = Integer.toString(LocalDate.now().getYear() + 2);
        return Stream.of(" ", "abcd", yearAfterNextYear, "21000");
    }

    private String randomValidYear() {
        var bound = (LocalDate.now().getYear() + 1) - Year.of(Year.MIN_VALUE).getValue();
        return Integer.toString(Year.of(Year.MIN_VALUE).getValue() + randomInteger(bound));
    }

    protected static Stream<String> invalidIssn() {
        return Stream.of("123456789", "1234-12XX", "1", "kdsnf0392ujrkijdf");
    }

}
