package no.sikt.nva.pubchannels.handler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.model.Contexts;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.paths.UriWrapper;

public record SerialPublicationDto(URI id, String identifier, String name, String onlineIssn, String printIssn,
                                   ScientificValue scientificValue, URI sameAs, String discontinued, String type,
                                   String year, ScientificValueReviewNotice reviewNotice) implements JsonSerializable {

    public static SerialPublicationDto create(URI selfUriBase,
                                              ThirdPartySerialPublication series,
                                              String requestedYear) {
        var year = Optional.ofNullable(series.getYear()).orElse(requestedYear);
        var id = UriWrapper.fromUri(selfUriBase).addChild(series.identifier(), year).getUri();
        return new SerialPublicationDto(id,
                                        series.identifier(),
                                        series.name(),
                                        series.onlineIssn(),
                                        series.printIssn(),
                                        series.getScientificValue(),
                                        series.homepage(),
                                        series.discontinued(),
                                        series.type(),
                                        year,
                                        series.reviewNotice());
    }

    @JsonProperty("@context")
    public URI getContext() {
        return URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    }
}
