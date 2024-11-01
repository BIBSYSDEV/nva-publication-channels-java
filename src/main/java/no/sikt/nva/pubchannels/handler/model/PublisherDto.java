package no.sikt.nva.pubchannels.handler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.net.URI;
import java.util.Optional;
import no.sikt.nva.pubchannels.handler.ScientificValue;
import no.sikt.nva.pubchannels.handler.ScientificValueReviewNotice;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.model.Contexts;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.paths.UriWrapper;

@JsonTypeName(PublisherDto.TYPE)
@JsonTypeInfo(use = Id.NAME, property = "type")
public record PublisherDto(URI id,
                           String identifier,
                           String name,
                           String isbnPrefix,
                           ScientificValue scientificValue,
                           URI sameAs,
                           String discontinued,
                           String year,
                           ScientificValueReviewNotice reviewNotice) implements JsonSerializable {

    public static final String TYPE = "Publisher";

    public static PublisherDto create(URI selfUriBase, ThirdPartyPublisher publisher, String requestedYear) {
        var year = Optional.ofNullable(publisher.getYear()).orElse(requestedYear);
        var id = UriWrapper.fromUri(selfUriBase).addChild(publisher.identifier(), year).getUri();
        return new PublisherDto(id,
                                publisher.identifier(),
                                publisher.name(),
                                publisher.isbnPrefix(),
                                publisher.getScientificValue(),
                                publisher.homepage(),
                                publisher.discontinued(),
                                year,
                                publisher.reviewNotice());
    }

    @JsonProperty("context")
    public URI getContext() {
        return URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
    }
}

