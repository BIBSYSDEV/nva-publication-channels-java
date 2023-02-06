package no.sikt.nva.pubchannels.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Objects;
import no.sikt.nva.pubchannels.Immutable;
import nva.commons.core.JacocoGenerated;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public final class Journal implements Immutable {

    private static final String CONTEXT_FIELD = "@context";
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String YEAR_FIELD = "year";
    private static final String ONLINE_ISSN_FIELD = "onlineIssn";
    private static final String PRINT_ISSN_FIELD = "printIssn";
    private static final String LEVEL_FIELD = "level";
    private static final String LANDING_PAGE_FIELD = "landingPage";

    @JsonProperty(CONTEXT_FIELD)
    private final URI context;
    @JsonProperty(ID_FIELD)
    private final URI id;
    @JsonProperty(IDENTIFIER_FIELD)
    private final String identifier;
    @JsonProperty(YEAR_FIELD)
    private final String year;
    @JsonProperty(NAME_FIELD)
    private final String name;
    @JsonProperty(ONLINE_ISSN_FIELD)
    private final String onlineIssn;
    @JsonProperty(PRINT_ISSN_FIELD)
    private final String printIssn;
    @JsonProperty(LEVEL_FIELD)
    private final String level;
    @JsonProperty(LANDING_PAGE_FIELD)
    private final URI landingPage;

    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public Journal(@JsonProperty(CONTEXT_FIELD) URI context,
                   @JsonProperty(ID_FIELD) URI id,
                   @JsonProperty(IDENTIFIER_FIELD) String identifier,
                   @JsonProperty(YEAR_FIELD) String year,
                   @JsonProperty(NAME_FIELD) String name,
                   @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                   @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                   @JsonProperty(LEVEL_FIELD) String level,
                   @JsonProperty(LANDING_PAGE_FIELD) URI landingPage) {

        this.context = context;
        this.id = id;
        this.identifier = identifier;
        this.year = year;
        this.name = name;
        this.onlineIssn = onlineIssn;
        this.printIssn = printIssn;
        this.level = level;
        this.landingPage = landingPage;
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

    public String getYear() {
        return year;
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

    public String getLevel() {
        return level;
    }

    public URI getLandingPage() {
        return landingPage;
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
        return Objects.equals(getContext(), journal.getContext())
               && Objects.equals(getId(), journal.getId())
               && Objects.equals(getIdentifier(), journal.getIdentifier())
               && Objects.equals(getYear(), journal.getYear())
               && Objects.equals(getName(), journal.getName())
               && Objects.equals(getOnlineIssn(), journal.getOnlineIssn())
               && Objects.equals(getPrintIssn(), journal.getPrintIssn())
               && Objects.equals(getLevel(), journal.getLevel())
               && Objects.equals(getLandingPage(), journal.getLandingPage());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getContext(),
                            getId(),
                            getIdentifier(),
                            getYear(),
                            getName(),
                            getOnlineIssn(),
                            getPrintIssn(),
                            getLevel(),
                            getLandingPage());
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return "Journal{"
               + "context=" + context
               + ", id=" + id
               + ", identifier='" + identifier + '\''
               + ", year='" + year + '\''
               + ", name='" + name + '\''
               + ", onlineIssn='" + onlineIssn + '\''
               + ", printIssn='" + printIssn + '\''
               + ", level='" + level + '\''
               + ", landingPage=" + landingPage
               + '}';
    }
}
