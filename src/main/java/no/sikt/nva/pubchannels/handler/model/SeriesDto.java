package no.sikt.nva.pubchannels.handler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.model.Contexts;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.paths.UriWrapper;

@JsonTypeName(SeriesDto.TYPE)
@JsonTypeInfo(use = Id.NAME, property = "type")
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

    public static SeriesDto create(URI selfUriBase, ThirdPartySeries series, String requestedYear) {
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

    @JsonProperty("context")
    public URI getContext() {
        return URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    }
}


