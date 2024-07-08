package no.sikt.nva.pubchannels.handler.fetch.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
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
    private static final String ONLINE_ISSN_FIELD = "onlineIssn";
    private static final String PRINT_ISSN_FIELD = "printIssn";
    private static final String SCIENTIFIC_VALUE_FIELD = "scientificValue";
    private static final String SAME_AS_FIELD = "sameAs";
    private static final String DISCONTINUED_FIELD = "discontinued";
    @JsonProperty(TYPE_FIELD)
    private static final String type = "Journal";
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

    @JsonCreator
    public FetchByIdAndYearResponse(@JsonProperty(ID_FIELD) URI id,
                                    @JsonProperty(IDENTIFIER_FIELD) String identifier,
                                    @JsonProperty(NAME_FIELD) String name,
                                    @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                                    @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                                    @JsonProperty(SCIENTIFIC_VALUE_FIELD) ScientificValue scientificValue,
                                    @JsonProperty(SAME_AS_FIELD) URI sameAs,
                                    @JsonProperty(DISCONTINUED_FIELD) String discontinued) {
        this.id = id;
        this.identifier = identifier;
        this.name = name;
        this.onlineIssn = onlineIssn;
        this.printIssn = printIssn;
        this.scientificValue = scientificValue;
        this.sameAs = sameAs;
        this.discontinued = discontinued;
    }

    public static FetchByIdAndYearResponse create(URI selfUriBase, ThirdPartyJournal journal, String requestedYear) {
        var year = Optional.ofNullable(journal.getYear()).orElse(requestedYear);
        var id = UriWrapper.fromUri(selfUriBase)
                     .addChild(journal.identifier(), year)
                     .getUri();
        return new FetchByIdAndYearResponse(id,
                                            journal.identifier(),
                                            journal.name(),
                                            journal.onlineIssn(),
                                            journal.printIssn(),
                                            journal.getScientificValue(),
                                            journal.homepage(),
                                            journal.discontinued());
    }

    public String getType() {
        return type;
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

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(context, id, identifier, name, onlineIssn, printIssn, scientificValue, sameAs,
                            discontinued);
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
               && Objects.equals(onlineIssn, that.onlineIssn)
               && Objects.equals(printIssn, that.printIssn)
               && scientificValue == that.scientificValue
               && Objects.equals(sameAs, that.sameAs)
               && Objects.equals(discontinued, that.discontinued);
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return toJsonString();
    }
}
