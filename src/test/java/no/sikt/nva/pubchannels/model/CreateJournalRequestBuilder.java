package no.sikt.nva.pubchannels.model;

public class CreateJournalRequestBuilder {
    private String name;
    private String pissn = null;
    private String eissn = null;

    public CreateJournalRequestBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CreateJournalRequestBuilder pissn(String pissn) {
        this.pissn = pissn;
        return this;
    }

    public CreateJournalRequestBuilder eissn(String eissn) {
        this.eissn = eissn;
        return this;
    }

    public CreateJournalRequest createCreateJournalRequest() {
        return new CreateJournalRequest(name, pissn, eissn);
    }
}