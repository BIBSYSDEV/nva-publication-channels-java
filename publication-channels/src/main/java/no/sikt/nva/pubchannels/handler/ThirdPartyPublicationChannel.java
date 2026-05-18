package no.sikt.nva.pubchannels.handler;

import java.net.URI;
import java.util.Optional;

public interface ThirdPartyPublicationChannel {

  String identifier();

  Optional<String> getYear();

  String name();

  ScientificValue getScientificValue();

  URI homepage();

  String discontinued();

  ScientificValueReviewNotice reviewNotice();

  String type();
}
