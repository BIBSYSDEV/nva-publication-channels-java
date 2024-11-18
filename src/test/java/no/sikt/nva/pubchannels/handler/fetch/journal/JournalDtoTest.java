package no.sikt.nva.pubchannels.handler.fetch.journal;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.model.JournalDto;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

class JournalDtoTest {

    @Test
    void canSerializeDeserializeJournalWithoutLossOfData() throws JsonProcessingException {
        var journal = randomJournal();

        var serializedJournal = dtoObjectMapper.writeValueAsString(journal);

        var deserializedJournal = dtoObjectMapper.readValue(serializedJournal, JournalDto.class);

        assertEquals(deserializedJournal, journal);
    }

    @Test
    void shouldSerializeWithJsonLdContext() throws JsonProcessingException {
        var serializedJournal = dtoObjectMapper.writeValueAsString(randomJournal());

        assertTrue(objectMapper.readTree(serializedJournal).has("@context"));
    }

    private static JournalDto randomJournal() {
        var journal =
                new ThirdPartySerialPublication() {

                    @Override
                    public String identifier() {
                        return randomString();
                    }

                    @Override
                    public String getYear() {
                        return randomString();
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
                        return new ScientificValueReviewNotice(
                                Map.of(randomString(), randomString()));
                    }

                    @Override
                    public String onlineIssn() {
                        return randomString();
                    }

                    @Override
                    public String printIssn() {
                        return randomString();
                    }
                };
        return JournalDto.create(randomUri(), journal, randomString());
    }
}
