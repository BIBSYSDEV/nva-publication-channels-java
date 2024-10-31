package no.sikt.nva.pubchannels.handler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.model.Contexts;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.paths.UriWrapper;

@JsonTypeName(JournalDto.TYPE)
@JsonTypeInfo(use = Id.NAME, property = "type")
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
                              null);
    }

    @JsonProperty("context")
    public URI getContext() {
        return URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    }
}
