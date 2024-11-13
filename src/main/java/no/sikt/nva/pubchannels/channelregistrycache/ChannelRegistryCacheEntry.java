package no.sikt.nva.pubchannels.channelregistrycache;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import com.opencsv.bean.CsvBindByName;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySeries;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;

public class ChannelRegistryCacheEntry {

    public static final String NULL = "0";
    @CsvBindByName(column = "underordnetPID")
    private String secondaryPid;
    @CsvBindByName(column = "PID")
    private String pid;
    @CsvBindByName(column = "type")
    private String type;
    @CsvBindByName(column = "Original tittel")
    private String originalTitle;
    @CsvBindByName(column = "Print ISSN")
    private String printIssn;
    @CsvBindByName(column = "Online ISSN")
    private String onlineIssn;
    @CsvBindByName(column = "ISBN-prefiks")
    private String isbn;
    @CsvBindByName(column = "Gjeldende nivå")
    private String currentLevel;
    @CsvBindByName(column = "Nedlagt")
    private String ceased;
    @CsvBindByName(column = "Nivåhistorikk")
    private String levelHistory;
    @CsvBindByName(column = "KURL")
    private String uri;

    @JacocoGenerated
    public String getSecondaryPid() {
        return secondaryPid;
    }

    @JacocoGenerated
    public String getPid() {
        return pid;
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
    public String getCeased() {
        return ceased.equals(NULL) ? null : ceased;
    }

    @JacocoGenerated
    public URI getUri() {
        return URI.create(uri);
    }

    public LevelForYear getCurrentLevel() {
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(levelHistory, LevelForYear.class)).orElseThrow();
    }

    public List<LevelForYear> getLevelHistory() {
        return nonNull(levelHistory) ? parseLevels() : List.of();
    }

    public ThirdPartyPublicationChannel toThirdPartyPublicationChannel(ChannelType type, String year) {
        return switch (type) {
            case JOURNAL -> toJournal(year);
            case SERIES -> toSeries(year);
            case PUBLISHER -> toPublisher(year);
        };
    }

    public ThirdPartyPublisher toPublisher(String year) {
        return new ChannelRegistryPublisher(getPid(), getChannelRegistryLevel(year), getIsbn(), getOriginalTitle(),
                                            getUri(), getCeased());
    }

    public ThirdPartySeries toSeries(String year) {
        return new ChannelRegistrySeries(getPid(), getOriginalTitle(), getOnlineIssn(), getPrintIssn(),
                                         getChannelRegistryLevel(year), getUri(), getCeased());
    }

    public ThirdPartyJournal toJournal(String year) {
        return new ChannelRegistryJournal(getPid(), getOriginalTitle(), getOnlineIssn(), getPrintIssn(),
                                          getChannelRegistryLevel(year), getUri(), getCeased());
    }

    private List<LevelForYear> parseLevels() {
        return attempt(
            () -> JsonUtils.dtoObjectMapper.readValue(toArrayString(levelHistory), LevelForYear[].class)).map(
            Arrays::asList).orElseThrow();
    }

    private ChannelRegistryLevel getChannelRegistryLevel(String year) {
        return new ChannelRegistryLevel(Integer.parseInt(year), getLevelForYear(year), null, null, null);
    }

    private String getLevelForYear(String year) {
        return getLevelHistory().stream()
                   .filter(levelForYear -> levelForYear.year().equals(year))
                   .map(LevelForYear::level)
                   .findFirst()
                   .orElse(null);
    }

    private String toArrayString(String value) {
        return String.format("[%s]", value);
    }
}
