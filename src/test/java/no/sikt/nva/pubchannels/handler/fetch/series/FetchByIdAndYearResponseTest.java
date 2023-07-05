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
import org.junit.jupiter.api.Test;

class FetchByIdAndYearResponseTest {

    @Test
    void canSerializeDeserializeSeriesWithoutLossOfData() throws JsonProcessingException {
        var series = randomSeries();

        var serializedSeries = dtoObjectMapper.writeValueAsString(series);

        var deserializedSeries = dtoObjectMapper.readValue(serializedSeries, FetchByIdAndYearResponse.class);

        assertThat(deserializedSeries, is(equalTo(series)));
    }

    private static FetchByIdAndYearResponse randomSeries() {
        var series = new ThirdPartySeries() {

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
        return FetchByIdAndYearResponse.create(randomUri(), series);
    }
}
