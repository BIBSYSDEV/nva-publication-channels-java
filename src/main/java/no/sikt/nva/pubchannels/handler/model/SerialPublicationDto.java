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

public record SerialPublicationDto(
    URI id,
    String identifier,
    String name,
    String onlineIssn,
    String printIssn,
    ScientificValue scientificValue,
    URI sameAs,
    String discontinued,
    String type,
    String year,
    ScientificValueReviewNotice reviewNotice)
    implements JsonSerializable {

  public static SerialPublicationDto create(
      URI selfUriBase, ThirdPartySerialPublication channel, String requestedYear) {
    var year = Optional.ofNullable(channel.getYear()).orElse(requestedYear);
    var id = UriWrapper.fromUri(selfUriBase).addChild(channel.identifier(), year).getUri();
    return new SerialPublicationDto(
        id,
        channel.identifier(),
        channel.name(),
        channel.onlineIssn(),
        channel.printIssn(),
        channel.getScientificValue(),
        channel.homepage(),
        channel.discontinued(),
        channel.type(),
        year,
        channel.reviewNotice());
  }

  @JsonProperty("@context")
  public URI getContext() {
    return URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT);
  }
}
