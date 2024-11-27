package no.sikt.nva.pubchannels.handler.create.journal;

public record CreateSerialPublicationRequest(String name,
                                             String printIssn,
                                             String onlineIssn,
                                             String homepage) {

}
