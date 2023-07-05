package no.sikt.nva.pubchannels.handler;

public interface ThirdPartyJournal extends ThirdPartyPublicationChannel {

    String getOnlineIssn();

    String getPrintIssn();
}
