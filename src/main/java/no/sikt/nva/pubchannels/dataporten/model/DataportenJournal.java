package no.sikt.nva.pubchannels.dataporten.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;

public final class DataportenJournal implements Immutable, ThirdPartyJournal {

    private static final String IDENTIFIER_FIELD = "pid";
    private static final String NAME_FIELD = "originalTitle";
    private static final String ONLINE_ISSN_FIELD = "eissn";
    private static final String PRINT_ISSN_FIELD = "pissn";
    private static final String LEVEL_FIELD = "levelElementDto";
    private static final String HOMEPAGE_FIELD = "kurl";

    @JsonProperty(ONLINE_ISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(LEVEL_FIELD)
    private final DataportenLevel dataportenLevel;
    @JsonProperty(PRINT_ISSN_FIELD)
    private final String printIssn;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(HOMEPAGE_FIELD)
    private final URI homepage;

    @JsonCreator
    public DataportenJournal(@JsonProperty(IDENTIFIER_FIELD) String identifier, @JsonProperty(NAME_FIELD) String name,
                             @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                             @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                             @JsonProperty(LEVEL_FIELD) DataportenLevel dataportenLevel,
                             @JsonProperty(HOMEPAGE_FIELD) URI homepage) {
        this.dataportenLevel = dataportenLevel;
        this.onlineIssn = onlineIssn;
        this.identifier = identifier;
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
        return Optional.ofNullable(getDataportenLevel())
                   .map(DataportenLevel::getYear)
                   .map(String::valueOf)
                   .orElse(null);
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

    public DataportenLevel getDataportenLevel() {
        return dataportenLevel;
    }

    private ScientificValue levelToScientificValue(ScientificValueMapper mapper) {
        return Optional.ofNullable(getDataportenLevel())
                   .map(level -> mapper.map(level.getLevel()))
                   .orElse(ScientificValue.UNASSIGNED);
    }
}
