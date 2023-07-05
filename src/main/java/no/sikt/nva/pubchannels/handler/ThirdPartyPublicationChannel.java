package no.sikt.nva.pubchannels.handler;

import java.net.URI;

public interface ThirdPartyPublicationChannel {

    String getIdentifier();

    String getYear();

    String getName();

    ScientificValue getScientificValue();

    URI getHomepage();
}
