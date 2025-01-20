package no.sikt.nva.pubchannels.handler;

public interface ThirdPartySerialPublication extends ThirdPartyPublicationChannel {

  String onlineIssn();

  String printIssn();
}
