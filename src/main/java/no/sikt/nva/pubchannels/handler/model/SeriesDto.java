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

public record SeriesDto(
    URI id,
    String identifier,
    String name,
    String onlineIssn,
    String printIssn,
    ScientificValue scientificValue,
    URI sameAs,
    String discontinued,
    String year,
    ScientificValueReviewNotice reviewNotice) implements JsonSerializable {

    public static final String TYPE = "Series";

    public static SeriesDto create(
        URI selfUriBase, ThirdPartySerialPublication series, String requestedYear) {
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
                             year,
                             series.reviewNotice());
    }

    @JsonProperty("type")
    public String getType() {
        return TYPE;
    }

    @JsonProperty("@context")
    public URI getContext() {
        return URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    }
}


