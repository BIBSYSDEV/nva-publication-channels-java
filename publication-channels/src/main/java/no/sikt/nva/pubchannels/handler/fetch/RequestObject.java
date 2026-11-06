package no.sikt.nva.pubchannels.handler.fetch;

import static nva.commons.core.attempt.Try.attempt;

import java.util.Optional;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.handler.validator.Validator;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

public record RequestObject(ChannelType channelType, String identifier, String year) {

  public static RequestObject fromRequestInfo(RequestInfo requestInfo) throws BadRequestException {
    var identifier = getIdentifier(requestInfo);
    var year = getYear(requestInfo);
    var type = getType(requestInfo);
    var requestObject = new RequestObject(type, identifier, year);
    requestObject.validate(new Validator());
    return requestObject;
  }

  public void validate(Validator validator) throws BadRequestException {
    try {
      validator.validate(this);
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  public Optional<String> getYear() {
    return Optional.ofNullable(year);
  }

  private static ChannelType getType(RequestInfo requestInfo) {
    return ChannelType.fromNvaPathElement(requestInfo.getPathParameter("type").trim());
  }

  private static String getYear(RequestInfo requestInfo) {
    return attempt(() -> requestInfo.getPathParameter("year"))
        .map(String::trim)
        .orElse(failure -> null);
  }

  private static String getIdentifier(RequestInfo requestInfo) throws BadRequestException {
    return attempt(() -> requestInfo.getPathParameter("identifier").trim())
        .map(UUID::fromString)
        .map(UUID::toString)
        .map(String::toUpperCase)
        .orElseThrow(failure -> new BadRequestException("Invalid identifier"));
  }
}
