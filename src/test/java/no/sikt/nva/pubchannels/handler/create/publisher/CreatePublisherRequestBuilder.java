package no.sikt.nva.pubchannels.handler.create.publisher;

public class CreatePublisherRequestBuilder {

    private String name;
    private String isbnPrefix;
    private String homepage;

    public CreatePublisherRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreatePublisherRequest build() {
        return new CreatePublisherRequest(name, isbnPrefix, homepage);
    }

    public CreatePublisherRequestBuilder withIsbnPrefix(String isbnPrefix) {
        this.isbnPrefix = isbnPrefix;
        return this;
    }

    public CreatePublisherRequestBuilder withHomepage(String homepage) {
        this.homepage = homepage;
        return this;
    }
}
