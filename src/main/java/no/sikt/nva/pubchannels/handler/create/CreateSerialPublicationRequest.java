package no.sikt.nva.pubchannels.handler.create;

public record CreateSerialPublicationRequest(String name,
                                             String printIssn,
                                             String onlineIssn,
                                             String homepage,
                                             String type) {

}
