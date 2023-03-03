package no.sikt.nva.pubchannels.handler.create;

public class CreateJournalRequestBuilder {
    private String name;
    private String printIssn = null;
    private String onlineIssn = null;
    private String homepage = null;

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

    public CreateJournalRequestBuilder withHomepage(String homepage) {
        this.homepage = homepage;
        return this;
    }

    public CreateJournalRequest build() {
        return new CreateJournalRequest(name, printIssn, onlineIssn, homepage);
    }
}