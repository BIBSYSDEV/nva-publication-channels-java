package no.sikt.nva.pubchannels.handler.create;

public class CreateSerialPublicationRequestBuilder {

    private String name;
    private String printIssn = null;
    private String onlineIssn = null;
    private String homepage = null;
    private String type = null;

    public CreateSerialPublicationRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreateSerialPublicationRequestBuilder withPrintIssn(String printIssn) {
        this.printIssn = printIssn;
        return this;
    }

    public CreateSerialPublicationRequestBuilder withOnlineIssn(String onlineIssn) {
        this.onlineIssn = onlineIssn;
        return this;
    }

    public CreateSerialPublicationRequestBuilder withHomepage(String homepage) {
        this.homepage = homepage;
        return this;
    }

    public CreateSerialPublicationRequestBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public CreateSerialPublicationRequest build() {
        return new CreateSerialPublicationRequest(name, printIssn, onlineIssn, homepage, type);
    }
}