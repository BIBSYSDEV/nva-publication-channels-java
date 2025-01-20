package no.sikt.nva.pubchannels.handler.create;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.commons.json.JsonUtils;
import org.junit.jupiter.api.Test;

class CreateSerialPublicationRequestTest {

    @Test
    void shouldBeAbleToRoundTripRecordViaJackson() throws JsonProcessingException {
        var createJournalRequest =
            new CreateSerialPublicationRequest(
                randomString(), randomString(), randomString(), randomString(), randomString());
        var createJournalRequestString =
            JsonUtils.dtoObjectMapper.writeValueAsString(createJournalRequest);
        var createJournalRequestRoundTripped =
            JsonUtils.dtoObjectMapper.readValue(
                createJournalRequestString, CreateSerialPublicationRequest.class);
        assertThat(createJournalRequestRoundTripped, is(equalTo(createJournalRequest)));
    }
}
