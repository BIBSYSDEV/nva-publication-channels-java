package no.sikt.nva.pubchannels.handler.fetch.publisher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.model.Contexts;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

public class FetchByIdAndYearResponse implements JsonSerializable {

    private static final String TYPE_FIELD = "type";
    private static final String CONTEXT_FIELD = "@context";
    private static final String ID_FIELD = "id";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String NAME_FIELD = "name";
    private static final String ISBN_PREFIX_FIELD = "isbnPrefix";
    private static final String SCIENTIFIC_VALUE_FIELD = "scientificValue";
    private static final String SAME_AS_FIELD = "sameAs";
    private static final String DISCONTINUED_FIELD = "discontinued";
    @JsonProperty(TYPE_FIELD)
    private static final String TYPE = "Publisher";
    @JsonProperty(CONTEXT_FIELD)
    private final URI context = URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    @JsonProperty(ID_FIELD)
    private final URI id;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(ISBN_PREFIX_FIELD)
    private final String isbnPrefix;
    @JsonProperty(SCIENTIFIC_VALUE_FIELD)
    private final ScientificValue scientificValue;
    @JsonProperty(SAME_AS_FIELD)
    private final URI sameAs;
    @JsonProperty(DISCONTINUED_FIELD)
    private final String discontinued;

    @JsonCreator
    public FetchByIdAndYearResponse(@JsonProperty(ID_FIELD) URI id,
                                    @JsonProperty(IDENTIFIER_FIELD) String identifier,
                                    @JsonProperty(NAME_FIELD) String name,
                                    @JsonProperty(ISBN_PREFIX_FIELD) String isbnPrefix,
                                    @JsonProperty(SCIENTIFIC_VALUE_FIELD) ScientificValue scientificValue,
                                    @JsonProperty(SAME_AS_FIELD) URI sameAs,
                                    @JsonProperty(DISCONTINUED_FIELD) String discontinued) {
        this.id = id;
        this.identifier = identifier;
        this.name = name;
        this.isbnPrefix = isbnPrefix;
        this.scientificValue = scientificValue;
        this.sameAs = sameAs;
        this.discontinued = discontinued;
    }

    public static FetchByIdAndYearResponse create(URI selfUriBase, ThirdPartyPublisher publisher,
                                                  String requestedYear) {
        var year = Optional.ofNullable(publisher.getYear()).orElse(requestedYear);
        var id = UriWrapper.fromUri(selfUriBase)
                     .addChild(publisher.identifier(), year)
                     .getUri();
        return new FetchByIdAndYearResponse(id,
                                            publisher.identifier(),
                                            publisher.name(),
                                            publisher.isbnPrefix(),
                                            publisher.getScientificValue(),
                                            publisher.homepage(),
                                            publisher.discontinued());
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

    public String getIsbnPrefix() {
        return isbnPrefix;
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

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(context, id, identifier, name, isbnPrefix, scientificValue, sameAs, discontinued);
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
        FetchByIdAndYearResponse that = (FetchByIdAndYearResponse) o;
        return Objects.equals(context, that.context)
               && Objects.equals(id, that.id)
               && Objects.equals(identifier, that.identifier)
               && Objects.equals(name, that.name)
               && Objects.equals(isbnPrefix, that.isbnPrefix)
               && scientificValue == that.scientificValue
               && Objects.equals(sameAs, that.sameAs)
               && Objects.equals(discontinued, that.discontinued);
    }
}

