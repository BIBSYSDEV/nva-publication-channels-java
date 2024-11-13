package no.sikt.nva.pubchannels.channelregistrycache;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class ChannelRegistryCacheEntryTest {

    public static final String TEST_CSV = "src/test/resources/cache.csv";

    @Test
    void shouldParseCsvToListOfBeans() throws FileNotFoundException {
        var beans = getChannelRegistryCacheEntries();

        assertTrue(beans.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnLevelForYearAsObject() throws FileNotFoundException {
        var channelRegistryCacheEntry = getChannelRegistryCacheEntries().get(2);
        var levelForYearList = channelRegistryCacheEntry.getLevelHistory();

        assertNotNull(channelRegistryCacheEntry.getCurrentLevel());
        assertTrue(levelForYearList.stream().allMatch(Objects::nonNull));
    }

    private static List<ChannelRegistryCacheEntry> getChannelRegistryCacheEntries() throws FileNotFoundException {
        return new CsvToBeanBuilder<ChannelRegistryCacheEntry>(new FileReader(TEST_CSV)).withType(
                ChannelRegistryCacheEntry.class)
                   .withSeparator(';')
                   .withIgnoreEmptyLine(true)
                   .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                   .build()
                   .parse();
    }
}