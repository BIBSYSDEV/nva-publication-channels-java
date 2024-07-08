package no.sikt.nva.pubchannels.handler.fetch.series;

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
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.handler.model.SeriesDto;
import org.junit.jupiter.api.Test;

class SeriesDtoTest {

    @Test
    void canSerializeDeserializeSeriesWithoutLossOfData() throws JsonProcessingException {
        var series = randomSeries();

        var serializedSeries = dtoObjectMapper.writeValueAsString(series);

        var deserializedSeries = dtoObjectMapper.readValue(serializedSeries, SeriesDto.class);

        assertThat(deserializedSeries, is(equalTo(series)));
    }

    private static SeriesDto randomSeries() {
        var series = new ThirdPartySeries() {

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
            public String onlineIssn() {
                return randomString();
            }

            @Override
            public String printIssn() {
                return randomString();
            }
        };
        return SeriesDto.create(randomUri(), series, randomString());
    }
}
