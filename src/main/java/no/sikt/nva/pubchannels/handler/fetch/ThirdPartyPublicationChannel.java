package no.sikt.nva.pubchannels.handler.fetch;

import no.sikt.nva.pubchannels.handler.ScientificValue;

import java.net.URI;

public interface ThirdPartyPublicationChannel {

    String getIdentifier();

    String getYear();

    String getName();

    String getOnlineIssn();

    String getPrintIssn();

    ScientificValue getScientificValue();

    URI getHomepage();
}
