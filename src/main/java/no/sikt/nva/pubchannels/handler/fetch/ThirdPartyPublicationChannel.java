package no.sikt.nva.pubchannels.handler.fetch;

import java.net.URI;
import no.sikt.nva.pubchannels.handler.ScientificValue;

public interface ThirdPartyPublicationChannel {

    String getIdentifier();

    String getYear();

    String getName();

    ScientificValue getScientificValue();

    URI getHomepage();
}
