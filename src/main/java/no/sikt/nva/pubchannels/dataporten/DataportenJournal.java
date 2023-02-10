package no.sikt.nva.pubchannels.dataporten;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.dataporten.mapper.Mapper;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;

final class DataportenJournal implements Immutable, ThirdPartyJournal {
    private static final String YEAR_FIELD = "Year";
    private static final String ONLINE_ISSN_FIELD = "Eissn";
    private static final String IDENTIFIER_FIELD = "Pid";
    private static final String SCIENTIFIC_VALUE_FIELD = "Level";
    private static final String PRINT_ISSN_FIELD = "Pissn";
    private static final String NAME_FIELD = "Name";
    private static final String HOMEPAGE_FIELD = "KURL";

    @JsonProperty(YEAR_FIELD)
    private final String year;
    @JsonProperty(ONLINE_ISSN_FIELD)
    private final transient String onlineIssn;
    @JsonProperty(IDENTIFIER_FIELD)
    private final transient String identifier;
    @JsonProperty(SCIENTIFIC_VALUE_FIELD)
    private final transient String scientificValue;
    @JsonProperty(PRINT_ISSN_FIELD)
    private final transient String printIssn;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(HOMEPAGE_FIELD)
    private final transient URI homepage;

    @JsonCreator
    public DataportenJournal(@JsonProperty(YEAR_FIELD) String year,
                             @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                             @JsonProperty(IDENTIFIER_FIELD) String identifier,
                             @JsonProperty(SCIENTIFIC_VALUE_FIELD) String scientificValue,
                             @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                             @JsonProperty(NAME_FIELD) String name,
                             @JsonProperty(HOMEPAGE_FIELD) URI homepage) {
        this.year = year;
        this.onlineIssn = onlineIssn;
        this.identifier = identifier;
        this.scientificValue = scientificValue;
        this.printIssn = printIssn;
        this.name = name;
        this.homepage = homepage;
    }

    @Override
    public String getIdentifier() {
        return identifier;
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
        return levelToScientificValue(new ScientificValueMapper());
    }

    @Override
    public URI getHomepage() {
        return homepage;
    }

    @Override
    public String getOnlineIssn() {
        return onlineIssn;
    }

    @Override
    public String getPrintIssn() {
        return printIssn;
    }

    private ScientificValue levelToScientificValue(Mapper mapper) {
        return mapper.map(scientificValue);
    }
}
