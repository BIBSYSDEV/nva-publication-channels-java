package no.sikt.nva.pubchannels.handler.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.model.Contexts;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

public class SeriesDto implements JsonSerializable {

    private static final String TYPE_FIELD = "type";
    private static final String CONTEXT_FIELD = "@context";
    private static final String ID_FIELD = "id";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String NAME_FIELD = "name";
    private static final String ONLINE_ISSN_FIELD = "onlineIssn";
    private static final String PRINT_ISSN_FIELD = "printIssn";
    private static final String SCIENTIFIC_VALUE_FIELD = "scientificValue";
    private static final String SAME_AS_FIELD = "sameAs";
    private static final String DISCONTINUED_FIELD = "discontinued";
    private static final String YEAR_FIELD = "year";

    @JsonProperty(TYPE_FIELD)
    private static final String TYPE = "Series";
    @JsonProperty(CONTEXT_FIELD)
    private final URI context = URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    @JsonProperty(ID_FIELD)
    private final URI id;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(ONLINE_ISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(PRINT_ISSN_FIELD)
    private final String printIssn;
    @JsonProperty(SCIENTIFIC_VALUE_FIELD)
    private final ScientificValue scientificValue;
    @JsonProperty(SAME_AS_FIELD)
    private final URI sameAs;
    @JsonProperty(DISCONTINUED_FIELD)
    private final String discontinued;
    @JsonProperty(YEAR_FIELD)
    private final String year;

    @JsonCreator
    public SeriesDto(@JsonProperty(ID_FIELD) URI id,
                     @JsonProperty(IDENTIFIER_FIELD) String identifier,
                     @JsonProperty(NAME_FIELD) String name,
                     @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                     @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                     @JsonProperty(SCIENTIFIC_VALUE_FIELD) ScientificValue scientificValue,
                     @JsonProperty(SAME_AS_FIELD) URI sameAs,
                     @JsonProperty(DISCONTINUED_FIELD) String discontinued,
                     @JsonProperty(YEAR_FIELD) String year) {
        this.id = id;
        this.identifier = identifier;
        this.name = name;
        this.onlineIssn = onlineIssn;
        this.printIssn = printIssn;
        this.scientificValue = scientificValue;
        this.sameAs = sameAs;
        this.discontinued = discontinued;
        this.year = year;
    }

    public static SeriesDto create(URI selfUriBase, ThirdPartySeries series, String requestedYear) {
        var year = Optional.ofNullable(series.getYear()).orElse(requestedYear);
        var id = UriWrapper.fromUri(selfUriBase).addChild(series.identifier(), year).getUri();
        return new SeriesDto(id,
                             series.identifier(),
                             series.name(),
                             series.onlineIssn(),
                             series.printIssn(),
                             series.getScientificValue(),
                             series.homepage(),
                             series.discontinued(),
                             year);
    }

    public String getType() {
        return TYPE;
    }

    public URI getContext() {
        return context;
    }

    public URI getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getOnlineIssn() {
        return onlineIssn;
    }

    public String getPrintIssn() {
        return printIssn;
    }

    public ScientificValue getScientificValue() {
        return scientificValue;
    }

    public URI getSameAs() {
        return sameAs;
    }

    public String getDiscontinued() {
        return discontinued;
    }

    public String getYear() {
        return year;
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(context,
                            id,
                            identifier,
                            name,
                            onlineIssn,
                            printIssn,
                            scientificValue,
                            sameAs,
                            discontinued,
                            year);
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeriesDto that = (SeriesDto) o;
        return Objects.equals(context, that.context)
               && Objects.equals(id, that.id)
               && Objects.equals(identifier,
                                 that.identifier)
               && Objects.equals(name, that.name)
               && Objects.equals(onlineIssn, that.onlineIssn)
               && Objects.equals(printIssn, that.printIssn)
               && scientificValue == that.scientificValue
               && Objects.equals(sameAs, that.sameAs)
               && Objects.equals(discontinued, that.discontinued)
               && Objects.equals(year, that.year);
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return toJsonString();
    }
}


