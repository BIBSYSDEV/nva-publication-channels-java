package no.sikt.nva.pubchannels.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public final class Journal implements Immutable {

    private static final String CONTEXT_FIELD = "@context";
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String ELECTRONIC_ISSN_FIELD = "eIssn";
    private static final String ISSN_FIELD = "issn";
    private static final String ACTIVE_FIELD = "active";
    private static final String LANGUAGE_FIELD = "language";
    private static final String WEBPAGE_FIELD = "webpage";
    private static final String PUBLISHER_FIELD = "publisher";
    private static final String NPI_DOMAIN_FIELD = "npiDomain";
    private static final String SCIENTIFIC_VALUE_FIELD = "scientificValue";

    @JsonProperty(CONTEXT_FIELD)
    private final URI context;
    @JsonProperty(ID_FIELD)
    private final URI id;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(ELECTRONIC_ISSN_FIELD)
    private final String electronicIssn;
    @JsonProperty(ISSN_FIELD)
    private final String issn;
    @JsonProperty(ACTIVE_FIELD)
    private final boolean active;
    @JsonProperty(LANGUAGE_FIELD)
    private final URI language;
    @JsonProperty(WEBPAGE_FIELD)
    private final URI webpage;
    @JsonProperty(PUBLISHER_FIELD)
    private final URI publisher;
    @JsonProperty(NPI_DOMAIN_FIELD)
    private final URI npiDomain;
    @JsonProperty(SCIENTIFIC_VALUE_FIELD)
    private final ScientificValue scientificValue;

    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public Journal(@JsonProperty(CONTEXT_FIELD) URI context,
                   @JsonProperty(ID_FIELD) URI id,
                   @JsonProperty(IDENTIFIER_FIELD) String identifier,
                   @JsonProperty(NAME_FIELD) String name,
                   @JsonProperty(ELECTRONIC_ISSN_FIELD) String electronicIssn,
                   @JsonProperty(ISSN_FIELD) String issn,
                   @JsonProperty(ACTIVE_FIELD) boolean active,
                   @JsonProperty(LANGUAGE_FIELD) URI language,
                   @JsonProperty(WEBPAGE_FIELD) URI webpage,
                   @JsonProperty(PUBLISHER_FIELD) URI publisher,
                   @JsonProperty(NPI_DOMAIN_FIELD) URI npiDomain,
                   @JsonProperty(SCIENTIFIC_VALUE_FIELD) ScientificValue scientificValue) {

        this.context = context;
        this.id = id;
        this.identifier = identifier;
        this.name = name;
        this.electronicIssn = electronicIssn;
        this.issn = issn;
        this.active = active;
        this.language = language;
        this.webpage = webpage;
        this.publisher = publisher;
        this.npiDomain = npiDomain;
        this.scientificValue = scientificValue;
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

    public String getElectronicIssn() {
        return electronicIssn;
    }

    public String getIssn() {
        return issn;
    }

    public boolean isActive() {
        return active;
    }

    public URI getLanguage() {
        return language;
    }

    public URI getWebpage() {
        return webpage;
    }

    public URI getPublisher() {
        return publisher;
    }

    public URI getNpiDomain() {
        return npiDomain;
    }

    public ScientificValue getScientificValue() {
        return scientificValue;
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
        Journal journal = (Journal) o;
        return isActive() == journal.isActive()
               && Objects.equals(getContext(), journal.getContext())
               && Objects.equals(getId(), journal.getId())
               && Objects.equals(getIdentifier(), journal.getIdentifier())
               && Objects.equals(getName(), journal.getName())
               && Objects.equals(getElectronicIssn(), journal.getElectronicIssn())
               && Objects.equals(getIssn(), journal.getIssn())
               && Objects.equals(getLanguage(), journal.getLanguage())
               && Objects.equals(getWebpage(), journal.getWebpage())
               && Objects.equals(getPublisher(), journal.getPublisher())
               && Objects.equals(getNpiDomain(), journal.getNpiDomain())
               && getScientificValue() == journal.getScientificValue();
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getIdentifier(), getName(), getElectronicIssn(), getIssn(),
                            isActive(), getLanguage(), getWebpage(), getPublisher(), getNpiDomain(),
                            getScientificValue());
    }
}
