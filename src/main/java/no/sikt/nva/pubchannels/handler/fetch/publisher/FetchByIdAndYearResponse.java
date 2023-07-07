package no.sikt.nva.pubchannels.handler.fetch.publisher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.model.Contexts;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

public class FetchByIdAndYearResponse {

    private static final String TYPE_FIELD = "type";
    private static final String CONTEXT_FIELD = "@context";
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String ISBN_PREFIX_FIELD = "isbnPrefix";
    private static final String SCIENTIFIC_VALUE_FIELD = "scientificValue";
    private static final String SAME_AS_FIELD = "sameAs";

    @JsonProperty(TYPE_FIELD)
    private static final String TYPE = "Publisher";
    @JsonProperty(CONTEXT_FIELD)
    private final URI context = URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    @JsonProperty(ID_FIELD)
    private final URI id;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(ISBN_PREFIX_FIELD)
    private final String isbnPrefix;
    @JsonProperty(SCIENTIFIC_VALUE_FIELD)
    private final ScientificValue scientificValue;
    @JsonProperty(SAME_AS_FIELD)
    private final URI sameAs;

    @JsonCreator
    public FetchByIdAndYearResponse(@JsonProperty(ID_FIELD) URI id,
                                    @JsonProperty(NAME_FIELD) String name,
                                    @JsonProperty(ISBN_PREFIX_FIELD) String isbnPrefix,
                                    @JsonProperty(SCIENTIFIC_VALUE_FIELD) ScientificValue scientificValue,
                                    @JsonProperty(SAME_AS_FIELD) URI sameAs) {
        this.id = id;
        this.name = name;
        this.isbnPrefix = isbnPrefix;
        this.scientificValue = scientificValue;
        this.sameAs = sameAs;
    }

    public static FetchByIdAndYearResponse create(URI selfUriBase, ThirdPartyPublisher publisher,
                                                  String requestedYear) {
        var year = Optional.ofNullable(publisher.getYear()).orElse(requestedYear);
        var id = UriWrapper.fromUri(selfUriBase)
                     .addChild(publisher.getIdentifier(), year)
                     .getUri();
        return new FetchByIdAndYearResponse(id,
                                            publisher.getName(),
                                            publisher.getIsbnPrefix(),
                                            publisher.getScientificValue(),
                                            publisher.getHomepage());
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

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getType(),
                            getContext(),
                            getId(),
                            getName(),
                            getIsbnPrefix(),
                            getScientificValue(),
                            getSameAs());
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FetchByIdAndYearResponse that = (FetchByIdAndYearResponse) o;
        return Objects.equals(getType(), that.getType())
               && Objects.equals(getContext(), that.getContext())
               && Objects.equals(getId(), that.getId())
               && Objects.equals(getName(), that.getName())
               && Objects.equals(getIsbnPrefix(), that.getIsbnPrefix())
               && Objects.equals(getScientificValue(), that.getScientificValue())
               && Objects.equals(getSameAs(), that.getSameAs());
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return "FetchByIdAndYearResponse{"
               + "type='" + TYPE + '\''
               + ", context=" + context
               + ", id=" + id
               + ", name='" + name + '\''
               + ", isbnPrefix='" + isbnPrefix + '\''
               + ", scientificValue='" + scientificValue + '\''
               + ", sameAs=" + sameAs
               + '}';
    }
}

