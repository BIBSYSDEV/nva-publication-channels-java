package no.sikt.nva.pubchannels.handler;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.InputStream;
import java.util.Map;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.RequestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FetchJournalRequestTest {
    private FetchJournalRequest fetchJournalRequest;

    @BeforeEach
    void beforeEach() throws JsonProcessingException {
        final InputStream inputStream = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                                            .withPathParameters(Map.of(
                                                "identifier", "identifier",
                                                "year", "year"
                                            ))
                                            .build();

        fetchJournalRequest = new FetchJournalRequest(RequestInfo.fromRequest(inputStream));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAccessingIdentifierWithoutHavingValidated() {
        //noinspection ResultOfMethodCallIgnored
        assertThrows(IllegalStateException.class, () -> fetchJournalRequest.getIdentifier());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenAccessingYearWithoutHavingValidated() {
        //noinspection ResultOfMethodCallIgnored
        assertThrows(IllegalStateException.class, () -> fetchJournalRequest.getYear());
    }
}
