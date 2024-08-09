package no.sikt.nva.pubchannels.handler.create.series;


public class CreateSeriesRequestBuilder {
    private String name;
    private String printIssn;
    private String onlineIssn;
    private String homepage;

    public CreateSeriesRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreateSeriesRequest build() {
        return new CreateSeriesRequest(name,printIssn,onlineIssn,homepage);
    }

    public CreateSeriesRequestBuilder withPrintIssn(String issn) {
        this.printIssn = issn;
        return this;
    }

    public CreateSeriesRequestBuilder withOnlineIssn(String issn) {
        this.onlineIssn = issn;
        return this;
    }

    public CreateSeriesRequestBuilder withHomepage(String homepage) {
        this.homepage = homepage;
        return this;
    }
}
