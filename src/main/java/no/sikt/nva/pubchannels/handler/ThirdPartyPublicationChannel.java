package no.sikt.nva.pubchannels.handler;

import java.net.URI;

public interface ThirdPartyPublicationChannel {

    String identifier();

    String getYear();

    String name();

    ScientificValue getScientificValue();

    URI homepage();

    String discontinued();
}
