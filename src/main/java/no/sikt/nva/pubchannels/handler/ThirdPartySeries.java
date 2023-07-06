package no.sikt.nva.pubchannels.handler;

public interface ThirdPartySeries extends ThirdPartyPublicationChannel {

    String getOnlineIssn();

    String getPrintIssn();
}
