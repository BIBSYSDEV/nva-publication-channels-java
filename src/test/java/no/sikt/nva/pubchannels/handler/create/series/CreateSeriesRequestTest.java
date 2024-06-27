package no.sikt.nva.pubchannels.handler.create.series;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.commons.json.JsonUtils;
import org.junit.jupiter.api.Test;

class CreateSeriesRequestTest {

    @Test
    void shouldBeAbleToRoundTripRecordViaJackson() throws JsonProcessingException {
        var createSeriesRequest = new CreateSeriesRequest(randomString(), randomString(), randomString(),
                                                          randomString());
        var createSeriesRequestString = JsonUtils.dtoObjectMapper.writeValueAsString(createSeriesRequest);
        var createSeriesRequestRoundTripped = JsonUtils.dtoObjectMapper.readValue(createSeriesRequestString,
                                                                                  CreateSeriesRequest.class);
        assertThat(createSeriesRequestRoundTripped, is(equalTo(createSeriesRequest)));
    }
}