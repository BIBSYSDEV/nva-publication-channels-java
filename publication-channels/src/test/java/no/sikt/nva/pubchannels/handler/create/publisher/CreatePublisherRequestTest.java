package no.sikt.nva.pubchannels.handler.create.publisher;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.commons.json.JsonUtils;
import org.junit.jupiter.api.Test;

class CreatePublisherRequestTest {

  @Test
  void shouldBeAbleToRoundTripRecordViaJackson() throws JsonProcessingException {
    var createPublisherRequest =
        new CreatePublisherRequest(randomString(), randomString(), randomString());
    var createPublisherRequestString =
        JsonUtils.dtoObjectMapper.writeValueAsString(createPublisherRequest);
    var createPublisherRequestRoundTripped =
        JsonUtils.dtoObjectMapper.readValue(
            createPublisherRequestString, CreatePublisherRequest.class);
    assertThat(createPublisherRequestRoundTripped, is(equalTo(createPublisherRequest)));
  }
}
