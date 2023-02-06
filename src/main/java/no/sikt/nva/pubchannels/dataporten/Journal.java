package no.sikt.nva.pubchannels.dataporten;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import no.sikt.nva.pubchannels.Immutable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
/* default */ final class Journal implements Immutable {
    private static final String YEAR_FIELD = "Year";
    private static final String EISSN_FIELD = "Eissn";
    private static final String PID_FIELD = "Pid";
    private static final String LEVEL_FIELD = "Level";
    private static final String PISSN_FIELD = "Pissn";
    private static final String NAME_FIELD = "Name";
    private static final String KURL_FIELD = "KURL";

    @JsonProperty(YEAR_FIELD)
    private final String year;
    @JsonProperty(EISSN_FIELD)
    private final String eissn;
    @JsonProperty(PID_FIELD)
    private final String pid;
    @JsonProperty(LEVEL_FIELD)
    private final String level;
    @JsonProperty(PISSN_FIELD)
    private final String pissn;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(KURL_FIELD)
    private final URI kurl;

    @JsonCreator
    public Journal(@JsonProperty(YEAR_FIELD) String year,
                   @JsonProperty(EISSN_FIELD) String eissn,
                   @JsonProperty(PID_FIELD) String pid,
                   @JsonProperty(LEVEL_FIELD) String level,
                   @JsonProperty(PISSN_FIELD) String pissn,
                   @JsonProperty(NAME_FIELD) String name,
                   @JsonProperty(KURL_FIELD) URI kurl) {
        this.year = year;
        this.eissn = eissn;
        this.pid = pid;
        this.level = level;
        this.pissn = pissn;
        this.name = name;
        this.kurl = kurl;
    }

    public String getYear() {
        return year;
    }

    public String getEissn() {
        return eissn;
    }

    public String getPid() {
        return pid;
    }

    public String getLevel() {
        return level;
    }

    public String getPissn() {
        return pissn;
    }

    public String getName() {
        return name;
    }

    public URI getKurl() {
        return kurl;
    }
}
