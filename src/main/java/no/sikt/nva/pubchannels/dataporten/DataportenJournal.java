package no.sikt.nva.pubchannels.dataporten;

import static java.util.Objects.isNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;

/* default */ final class DataportenJournal implements Immutable, ThirdPartyJournal {
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
    private final transient String eissn;
    @JsonProperty(PID_FIELD)
    private final transient String pid;
    @JsonProperty(LEVEL_FIELD)
    private final transient String level;
    @JsonProperty(PISSN_FIELD)
    private final transient String pissn;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(KURL_FIELD)
    private final transient URI kurl;

    @JsonCreator
    public DataportenJournal(@JsonProperty(YEAR_FIELD) String year,
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

    @Override
    public String getIdentifier() {
        return pid;
    }

    @Override
    public String getYear() {
        return year;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ScientificValue getScientificValue() {
        return levelToScientificValue();
    }

    @Override
    public URI getLandingPage() {
        return kurl;
    }

    @Override
    public String getOnlineIssn() {
        return eissn;
    }

    @Override
    public String getPrintIssn() {
        return pissn;
    }

    private ScientificValue levelToScientificValue() {
        final ScientificValue scientificValue;
        if (isNull(this.level)) {
            scientificValue = ScientificValue.UNASSIGNED;
        } else {
            switch (this.level) {
                case "0":
                    scientificValue = ScientificValue.LEVEL_ZERO;
                    break;
                case "1":
                    scientificValue = ScientificValue.LEVEL_ONE;
                    break;
                case "2":
                    scientificValue = ScientificValue.LEVEL_TWO;
                    break;
                default:
                    scientificValue = ScientificValue.UNASSIGNED;
                    break;
            }
        }

        return scientificValue;
    }
}
