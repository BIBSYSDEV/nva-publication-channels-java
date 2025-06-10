package no.sikt.nva.pubchannels.handler.fetch.serialpublication;

import static nva.commons.core.attempt.Try.attempt;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import nva.commons.apigateway.RequestInfo;

public final class RequestObject {

  private final ChannelType type;
  private final UUID identifier;
  private final String year;

  public RequestObject(ChannelType type, UUID identifier, String year) {
    this.type = type;
    this.identifier = identifier;
    this.year = year;
  }

  public static RequestObject from(RequestInfo requestInfo) {
    var identifier = UUID.fromString(requestInfo.getPathParameter("identifier").trim());
    var year =
        attempt(() -> requestInfo.getPathParameter("year"))
            .map(String::trim)
            .orElse(failure -> null);
    var type = ChannelType.fromNvaPathElement(requestInfo.getPathParameter("type").trim());
    return new RequestObject(type, identifier, year);
  }

  public ChannelType type() {
    return type;
  }

  public UUID identifier() {
    return identifier;
  }

  public Optional<String> year() {
    return Optional.ofNullable(year);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, identifier, year);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (RequestObject) obj;
    return Objects.equals(this.type, that.type)
        && Objects.equals(this.identifier, that.identifier)
        && Objects.equals(this.year, that.year);
  }
}
