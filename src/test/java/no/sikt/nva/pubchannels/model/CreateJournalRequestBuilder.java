package no.sikt.nva.pubchannels.model;

public class CreateJournalRequestBuilder {
    private String name;
    private String printIssn = null;
    private String onlineIssn = null;
    private String url = null;

    public CreateJournalRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreateJournalRequestBuilder withPrintIssn(String printIssn) {
        this.printIssn = printIssn;
        return this;
    }

    public CreateJournalRequestBuilder withOnlineIssn(String onlineIssn) {
        this.onlineIssn = onlineIssn;
        return this;
    }

    public CreateJournalRequestBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public CreateJournalRequest build() {
        return new CreateJournalRequest(name, printIssn, onlineIssn, url);
    }
}