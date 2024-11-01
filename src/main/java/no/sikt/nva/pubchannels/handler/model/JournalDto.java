package no.sikt.nva.pubchannels.handler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.model.Contexts;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.paths.UriWrapper;

public record JournalDto(URI id,
                         String identifier,
                         String name,
                         String onlineIssn,
                         String printIssn,
                         ScientificValue scientificValue,
                         URI sameAs,
                         String discontinued,
                         String year,
                         ScientificValueReviewNotice reviewNotice) implements JsonSerializable {

    public static final String TYPE = "Journal";

    public static JournalDto create(URI selfUriBase, ThirdPartyJournal journal, String requestedYear) {
        var year = Optional.ofNullable(journal.getYear()).orElse(requestedYear);
        var id = UriWrapper.fromUri(selfUriBase).addChild(journal.identifier(), year).getUri();
        return new JournalDto(id,
                              journal.identifier(),
                              journal.name(),
                              journal.onlineIssn(),
                              journal.printIssn(),
                              journal.getScientificValue(),
                              journal.homepage(),
                              journal.discontinued(),
                              year,
                              journal.reviewNotice());
    }

    @JsonProperty("type")
    public String getType() {
        return TYPE;
    }

    @JsonProperty("context")
    public URI getContext() {
        return URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    }
}
