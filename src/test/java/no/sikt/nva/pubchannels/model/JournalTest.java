package no.sikt.nva.pubchannels.model;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public class JournalTest {

    @Test
    void canSerializeDeserializeJournalWithoutLossOfData() throws JsonProcessingException {
        var journal = randomJournal();

        var serializedJournal = dtoObjectMapper.writeValueAsString(journal);

        var deserializedJournal = dtoObjectMapper.readValue(serializedJournal, Journal.class);

        assertThat(deserializedJournal, is(equalTo(journal)));
    }

    private static Journal randomJournal() {
        return new Journal(randomUri(), randomUri(), randomString(), randomString(), randomString(), randomString(),
                           randomString(), randomElement("0", "1", "2"), randomUri());
    }
}
