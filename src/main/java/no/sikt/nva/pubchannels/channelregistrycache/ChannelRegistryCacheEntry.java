package no.sikt.nva.pubchannels.channelregistrycache;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import com.opencsv.bean.CsvBindByName;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySeries;
import no.sikt.nva.pubchannels.channelregistrycache.db.model.ChannelRegistryCacheDao;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;

public class ChannelRegistryCacheEntry {

    public static final String NULL = "0";
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
    @CsvBindByName(column = "Nedlagt")
    private String ceased;
    @CsvBindByName(column = "Nivåhistorikk")
    private String levelHistory;
    @CsvBindByName(column = "KURL")
    private String uri;

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
        return NULL.equals(ceased) ? null : ceased;
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

    public static Builder builder() {
        return new Builder();
    }

    public ThirdPartyPublicationChannel toThirdPartyPublicationChannel(ChannelType type, String year) {
        return switch (type) {
            case JOURNAL -> toJournal(year);
            case SERIES -> toSeries(year);
            case PUBLISHER -> toPublisher(year);
        };
    }

    public ChannelRegistryCacheDao toDao() {
        return ChannelRegistryCacheDao.builder()
                   .identifier(UUID.fromString(getPid()))
                   .type(getType())
                   .title(getOriginalTitle())
                   .printIssn(getPrintIssn())
                   .onlineIssn(getOnlineIssn())
                   .isbn(getIsbn())
                   .ceased(getCeased())
                   .levelHistory(getLevelHistory())
                   .uri(getUri())
                   .build();
    }

    public static ChannelRegistryCacheEntry fromDao(ChannelRegistryCacheDao dao) {
        return ChannelRegistryCacheEntry.builder()
                   .withPid(dao.identifier().toString())
                   .withType(dao.type())
                   .withOriginalTitle(dao.title())
                   .withPrintIssn(dao.printIssn())
                   .withOnlineIssn(dao.onlineIssn())
                   .withIsbn(dao.isbn())
                   .withCeased(dao.ceased())
                   .withLevelHistory(dao.levelHistory().isEmpty() ? null : dao.levelHistory().toString())
                   .withUri(dao.uri().toString())
                   .build();
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

    public static final class Builder {

        private String pid;
        private String type;
        private String originalTitle;
        private String printIssn;
        private String onlineIssn;
        private String isbn;
        private String ceased;
        private String levelHistory;
        private String uri;

        private Builder() {
        }

        public Builder withPid(String pid) {
            this.pid = pid;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withOriginalTitle(String originalTitle) {
            this.originalTitle = originalTitle;
            return this;
        }

        public Builder withPrintIssn(String printIssn) {
            this.printIssn = printIssn;
            return this;
        }

        public Builder withOnlineIssn(String onlineIssn) {
            this.onlineIssn = onlineIssn;
            return this;
        }

        public Builder withIsbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder withCeased(String ceased) {
            this.ceased = ceased;
            return this;
        }

        public Builder withLevelHistory(String levelHistory) {
            this.levelHistory = levelHistory;
            return this;
        }

        public Builder withUri(String uri) {
            this.uri = uri;
            return this;
        }

        public ChannelRegistryCacheEntry build() {
            ChannelRegistryCacheEntry channelRegistryCacheEntry = new ChannelRegistryCacheEntry();
            channelRegistryCacheEntry.printIssn = this.printIssn;
            channelRegistryCacheEntry.type = this.type;
            channelRegistryCacheEntry.isbn = this.isbn;
            channelRegistryCacheEntry.levelHistory = this.levelHistory;
            channelRegistryCacheEntry.uri = this.uri;
            channelRegistryCacheEntry.originalTitle = this.originalTitle;
            channelRegistryCacheEntry.ceased = this.ceased;
            channelRegistryCacheEntry.onlineIssn = this.onlineIssn;
            channelRegistryCacheEntry.pid = this.pid;
            return channelRegistryCacheEntry;
        }
    }
}
