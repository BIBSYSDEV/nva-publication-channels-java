package no.sikt.nva.pubchannels.handler.create.journal;

public record CreateJournalRequest(String name,
                                   String printIssn,
                                   String onlineIssn,
                                   String homepage) {

}
