package no.sikt.nva.pubchannels.dataporten.fetch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import no.sikt.nva.pubchannels.Immutable;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublisher;

public final class FetchPublisherByIdAndYearResponse implements Immutable, ThirdPartyPublisher {

    private static final String YEAR_FIELD = "year";
    private static final String IDENTIFIER_FIELD = "pid";
    private static final String SCIENTIFIC_VALUE_FIELD = "level";
    private static final String ISBN_PREFIX_FIELD = "isbnprefix";
    private static final String NAME_FIELD = "name";
    private static final String HOMEPAGE_FIELD = "kURL";

    @JsonProperty(YEAR_FIELD)
    private final String year;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(SCIENTIFIC_VALUE_FIELD)
    private final String scientificValue;
    @JsonProperty(ISBN_PREFIX_FIELD)
    private final String isbnPrefix;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(HOMEPAGE_FIELD)
    private final URI homepage;

    @JsonCreator
    public FetchPublisherByIdAndYearResponse(@JsonProperty(YEAR_FIELD) String year,
                                             @JsonProperty(IDENTIFIER_FIELD) String identifier,
                                             @JsonProperty(SCIENTIFIC_VALUE_FIELD) String scientificValue,
                                             @JsonProperty(ISBN_PREFIX_FIELD) String isbnPrefix,
                                             @JsonProperty(NAME_FIELD) String name,
                                             @JsonProperty(HOMEPAGE_FIELD) URI homepage) {
        this.year = year;
        this.identifier = identifier;
        this.scientificValue = scientificValue;
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
        return year;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIsbnPrefix() {
        return isbnPrefix;
    }

    @Override
    public ScientificValue getScientificValue() {
        return levelToScientificValue(new ScientificValueMapper());
    }

    @Override
    public URI getHomepage() {
        return homepage;
    }

    private ScientificValue levelToScientificValue(ScientificValueMapper mapper) {
        return mapper.map(scientificValue);
    }
}
