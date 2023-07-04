package no.sikt.nva.pubchannels.handler.fetch.journal;

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
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyJournal;
import org.junit.jupiter.api.Test;

class FetchByIdAndYearResponseTest {

    @Test
    void canSerializeDeserializeJournalWithoutLossOfData() throws JsonProcessingException {
        var journal = randomJournal();

        var serializedJournal = dtoObjectMapper.writeValueAsString(journal);

        var deserializedJournal = dtoObjectMapper.readValue(serializedJournal, FetchByIdAndYearResponse.class);

        assertThat(deserializedJournal, is(equalTo(journal)));
    }

    private static FetchByIdAndYearResponse randomJournal() {
        var journal = new ThirdPartyJournal() {

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
            public ScientificValue getScientificValue() {
                return randomElement(ScientificValue.values());
            }

            @Override
            public URI getHomepage() {
                return randomUri();
            }

            @Override
            public String getOnlineIssn() {
                return randomString();
            }

            @Override
            public String getPrintIssn() {
                return randomString();
            }
        };
        return FetchByIdAndYearResponse.create(randomUri(), journal);
    }
}
