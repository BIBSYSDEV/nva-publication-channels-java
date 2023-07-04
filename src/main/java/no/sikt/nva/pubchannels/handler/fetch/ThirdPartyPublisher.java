package no.sikt.nva.pubchannels.handler.fetch;

import java.net.URI;
import no.sikt.nva.pubchannels.handler.ScientificValue;

public interface ThirdPartyPublisher {

    String getIdentifier();

    String getYear();

    String getName();

    String getIsbnPrefix();

    ScientificValue getScientificValue();

    URI getHomepage();
}
