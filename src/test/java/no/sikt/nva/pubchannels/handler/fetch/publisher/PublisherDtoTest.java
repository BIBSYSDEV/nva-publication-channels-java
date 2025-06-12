package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import org.junit.jupiter.api.Test;

class PublisherDtoTest {

  @Test
  void canSerializeDeserializePublisherWithoutLossOfData() throws JsonProcessingException {
    var publisher = randomPublisher();

    var serializedPublisher = dtoObjectMapper.writeValueAsString(publisher);

    var deserializedPublisher = dtoObjectMapper.readValue(serializedPublisher, PublisherDto.class);

    assertThat(deserializedPublisher, is(equalTo(publisher)));
  }

  @Test
  void shouldSerializeWithJsonLdContext() throws JsonProcessingException {
    var serializedPublisher = dtoObjectMapper.writeValueAsString(randomPublisher());

    assertTrue(objectMapper.readTree(serializedPublisher).has("@context"));
  }

  private static PublisherDto randomPublisher() {
    var publisher =
        new ThirdPartyPublisher() {

          @Override
          public String identifier() {
            return randomString();
          }

          @Override
          public Optional<String> getYear() {
            return Optional.of(randomString());
          }

          @Override
          public String name() {
            return randomString();
          }

          @Override
          public ScientificValue getScientificValue() {
            return randomElement(ScientificValue.values());
          }

          @Override
          public URI homepage() {
            return randomUri();
          }

          @Override
          public String discontinued() {
            return randomString();
          }

          @Override
          public ScientificValueReviewNotice reviewNotice() {
            return new ScientificValueReviewNotice(Map.of(randomString(), randomString()));
          }

          @Override
          public String type() {
            return "publisher";
          }

          @Override
          public String isbnPrefix() {
            return randomString();
          }
        };
    return PublisherDto.create(randomUri(), publisher, randomString());
  }
}
