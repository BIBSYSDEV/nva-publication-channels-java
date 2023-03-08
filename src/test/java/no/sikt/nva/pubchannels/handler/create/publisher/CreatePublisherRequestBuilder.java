package no.sikt.nva.pubchannels.handler.create.publisher;

public class CreatePublisherRequestBuilder {
    private String name;
    private String printIssn;
    private String onlineIssn;
    private String homepage;

    public CreatePublisherRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreatePublisherRequest build() {
        return new CreatePublisherRequest(name,printIssn,onlineIssn,homepage);
    }

    public CreatePublisherRequestBuilder withPrintIssn(String issn) {
        this.printIssn = issn;
        return this;
    }

    public CreatePublisherRequestBuilder withOnlineIssn(String issn) {
        this.onlineIssn = issn;
        return this;
    }

    public CreatePublisherRequestBuilder withHomepage(String homepage) {
        this.homepage = homepage;
        return this;
    }
}
