package no.sikt.nva.pubchannels.handler;

import java.net.URI;

public interface ThirdPartyJournal {

    String getIdentifier();

    String getYear();

    String getName();

    String getOnlineIssn();

    String getPrintIssn();

    ScientificValue getScientificValue();

    URI getLandingPage();
}
