package no.sikt.nva.pubchannels.handler.fetch.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublicationChannel;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class FetchByIdAndYearResponseTest {

    @Test
    void canSerializeDeserializePublicherWithoutLossOfData() throws JsonProcessingException {
        var publisher = randomPublisher();

        var serializedPublisher = dtoObjectMapper.writeValueAsString(publisher);

        var deserializedPublisher = dtoObjectMapper.readValue(serializedPublisher, FetchByIdAndYearResponse.class);

        assertThat(deserializedPublisher, is(equalTo(publisher)));
    }

    private static FetchByIdAndYearResponse randomPublisher() {
        var publisher = new ThirdPartyPublicationChannel() {

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
            public String getOnlineIssn() {
                return randomString();
            }

            @Override
            public String getPrintIssn() {
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
        return FetchByIdAndYearResponse.create(randomUri(), publisher);
    }
}
