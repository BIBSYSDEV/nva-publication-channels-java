package no.sikt.nva.pubchannels.dataporten.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;

public final class DataportenPublisher implements Immutable, ThirdPartyPublisher {

    private static final String IDENTIFIER_FIELD = "pid";
    private static final String NAME_FIELD = "name";
    private static final String ISBN_PREFIX_FIELD = "isbnprefix";
    private static final String LEVEL_FIELD = "levelElementDto";
    private static final String HOMEPAGE_FIELD = "kurl";

    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(LEVEL_FIELD)
    private final DataportenLevel dataportenLevel;
    @JsonProperty(ISBN_PREFIX_FIELD)
    private final String isbnPrefix;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(HOMEPAGE_FIELD)
    private final URI homepage;

    @JsonCreator
    public DataportenPublisher(@JsonProperty(IDENTIFIER_FIELD) String identifier,
                               @JsonProperty(LEVEL_FIELD) DataportenLevel dataportenLevel,
                               @JsonProperty(ISBN_PREFIX_FIELD) String isbnPrefix,
                               @JsonProperty(NAME_FIELD) String name,
                               @JsonProperty(HOMEPAGE_FIELD) URI homepage) {
        this.identifier = identifier;
        this.dataportenLevel = dataportenLevel;
        this.isbnPrefix = isbnPrefix;
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
    public String getIsbnPrefix() {
        return isbnPrefix;
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
