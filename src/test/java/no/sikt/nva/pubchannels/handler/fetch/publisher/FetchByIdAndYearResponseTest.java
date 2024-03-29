package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import org.junit.jupiter.api.Test;

class FetchByIdAndYearResponseTest {

    @Test
    void canSerializeDeserializePublisherWithoutLossOfData() throws JsonProcessingException {
        var publisher = randomPublisher();

        var serializedPublisher = dtoObjectMapper.writeValueAsString(publisher);

        var deserializedPublisher = dtoObjectMapper.readValue(serializedPublisher, FetchByIdAndYearResponse.class);

        assertThat(deserializedPublisher, is(equalTo(publisher)));
    }

    private static FetchByIdAndYearResponse randomPublisher() {
        var publisher = new ThirdPartyPublisher() {

            @Override
            public String getIdentifier() {
                return randomString();
            }

            @Override
            public String getYear() {
                return randomString();
            }

            @Override
            public String getName() {
                return randomString();
            }

            @Override
            public String getIsbnPrefix() {
                return randomString();
            }

            @Override
            public ScientificValue getScientificValue() {
                return randomElement(ScientificValue.values());
            }

            @Override
            public URI getHomepage() {
                return randomUri();
            }
        };
        return FetchByIdAndYearResponse.create(randomUri(), publisher, randomString());
    }
}
