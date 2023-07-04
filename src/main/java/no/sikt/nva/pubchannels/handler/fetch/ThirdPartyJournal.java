package no.sikt.nva.pubchannels.handler.fetch;

public interface ThirdPartyJournal extends ThirdPartyPublicationChannel {

    String getOnlineIssn();

    String getPrintIssn();
}
