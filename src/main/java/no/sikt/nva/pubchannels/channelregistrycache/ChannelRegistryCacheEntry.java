package no.sikt.nva.pubchannels.channelregistrycache;

import static nva.commons.core.attempt.Try.attempt;
import com.opencsv.bean.CsvBindByName;
import java.util.Arrays;
import java.util.List;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;

public class ChannelRegistryCacheEntry {

    @CsvBindByName(column = "underordnetPID")
    private String secondaryPid;

    @CsvBindByName(column = "PID")
    private String pid;

    @CsvBindByName(column = "regDato")
    private String createdDate;

    @CsvBindByName(column = "type")
    private String type;

    @CsvBindByName(column = "Original tittel")
    private String originalTitle;

    @CsvBindByName(column = "Internasjonal tittel")
    private String internationalTitle;
    @CsvBindByName(column = "Print ISSN")
    private String printIssn;
    @CsvBindByName(column = "Online ISSN")
    private String onlineIssn;
    @CsvBindByName(column = "ISBN-prefiks")
    private String isbn;
    @CsvBindByName(column = "Publiseringsavtale")
    private String agreement;
    @CsvBindByName(column = "DOAJ")
    private String openAccessJournal;
    @CsvBindByName(column = "Gjeldende nivå")
    private String currentLevel;
    @CsvBindByName(column = "Nedlagt")
    private String closed;
    @CsvBindByName(column = "Nivåhistorikk")
    private String levelHistory;
    @CsvBindByName(column = "KURL")
    private String uri;

    public ChannelRegistryCacheEntry() {
        // NO-OP
    }

    @JacocoGenerated
    public String getSecondaryPid() {
        return secondaryPid;
    }

    @JacocoGenerated
    public String getPid() {
        return pid;
    }

    @JacocoGenerated
    public String getCreatedDate() {
        return createdDate;
    }

    @JacocoGenerated
    public String getType() {
        return type;
    }

    @JacocoGenerated
    public String getOriginalTitle() {
        return originalTitle;
    }

    @JacocoGenerated
    public String getInternationalTitle() {
        return internationalTitle;
    }

    @JacocoGenerated
    public String getPrintIssn() {
        return printIssn;
    }

    @JacocoGenerated
    public String getOnlineIssn() {
        return onlineIssn;
    }

    @JacocoGenerated
    public String getIsbn() {
        return isbn;
    }

    @JacocoGenerated
    public String getAgreement() {
        return agreement;
    }

    @JacocoGenerated
    public String getOpenAccessJournal() {
        return openAccessJournal;
    }

    @JacocoGenerated
    public String getClosed() {
        return closed;
    }

    @JacocoGenerated
    public String getUri() {
        return uri;
    }

    public LevelForYear getCurrentLevel() {
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(levelHistory, LevelForYear.class)).orElseThrow();
    }

    public List<LevelForYear> getLevelHistory() {
        return attempt(
            () -> JsonUtils.dtoObjectMapper.readValue(toArrayString(levelHistory), LevelForYear[].class)).map(
            Arrays::asList).orElseThrow();
    }

    private String toArrayString(String value) {
        return String.format("[%s]", value);
    }
}
