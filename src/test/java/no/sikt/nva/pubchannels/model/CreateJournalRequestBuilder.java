package no.sikt.nva.pubchannels.model;

public class CreateJournalRequestBuilder {
    private String name;
    private String printIssn = null;
    private String onlineIssn = null;
    private String url = null;

    public CreateJournalRequestBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CreateJournalRequestBuilder printIssn(String printIssn) {
        this.printIssn = printIssn;
        return this;
    }

    public CreateJournalRequestBuilder onlineIssn(String onlineIssn) {
        this.onlineIssn = onlineIssn;
        return this;
    }

    public CreateJournalRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    public CreateJournalRequest build() {
        return new CreateJournalRequest(name, printIssn, onlineIssn, url);
    }
}