package no.sikt.nva.pubchannels.handler.search.journal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.sikt.nva.pubchannels.dataporten.mapper.ScientificValueMapper;
import no.sikt.nva.pubchannels.dataporten.search.DataportenJournalResult;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.model.Contexts;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Objects;

public class JournalResult {
    private static final String TYPE_FIELD = "type";
    private static final String CONTEXT_FIELD = "@context";
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String ONLINE_ISSN_FIELD = "onlineIssn";
    private static final String PRINT_ISSN_FIELD = "printIssn";
    private static final String SCIENTIFIC_VALUE_FIELD = "scientificValue";
    private static final String SAME_AS_FIELD = "sameAs";

    @JsonProperty(TYPE_FIELD)
    private static final String type = "Journal";
    @JsonProperty(CONTEXT_FIELD)
    private final URI context = URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    @JsonProperty(ID_FIELD)
    private final URI id;
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

    @JsonCreator
    public JournalResult(@JsonProperty(ID_FIELD) URI id,
                         @JsonProperty(NAME_FIELD) String name,
                         @JsonProperty(ONLINE_ISSN_FIELD) String onlineIssn,
                         @JsonProperty(PRINT_ISSN_FIELD) String printIssn,
                         @JsonProperty(SCIENTIFIC_VALUE_FIELD) ScientificValue scientificValue,
                         @JsonProperty(SAME_AS_FIELD) URI sameAs) {
        this.id = id;
        this.name = name;
        this.onlineIssn = onlineIssn;
        this.printIssn = printIssn;
        this.scientificValue = scientificValue;
        this.sameAs = sameAs;
    }

    public static JournalResult create(URI selfUriBase, DataportenJournalResult journal) {
        var id = UriWrapper.fromUri(selfUriBase)
                .addChild(journal.getIdentifier(), String.valueOf(journal.getCurrentLevel().getYear()))
                .getUri();
        return new JournalResult(id,
                journal.getName(),
                journal.getOnlineIssn(),
                journal.getPrintIssn(),
                new ScientificValueMapper().map(journal.getCurrentLevel().getLevel()),
                journal.getHomepage());
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

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JournalResult that = (JournalResult) o;
        return Objects.equals(getType(), that.getType())
                && Objects.equals(getContext(), that.getContext())
                && Objects.equals(getId(), that.getId())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getOnlineIssn(), that.getOnlineIssn())
                && Objects.equals(getPrintIssn(), that.getPrintIssn())
                && Objects.equals(getScientificValue(), that.getScientificValue())
                && Objects.equals(getSameAs(), that.getSameAs());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getType(),
                getContext(),
                getId(),
                getName(),
                getOnlineIssn(),
                getPrintIssn(),
                getScientificValue(),
                getSameAs());
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return "SearchJournalResponse{"
                + "type='" + type + '\''
                + ", context=" + context
                + ", id=" + id
                + ", name='" + name + '\''
                + ", onlineIssn='" + onlineIssn + '\''
                + ", printIssn='" + printIssn + '\''
                + ", scientificValue='" + scientificValue + '\''
                + ", sameAs=" + sameAs
                + '}';
    }
}
