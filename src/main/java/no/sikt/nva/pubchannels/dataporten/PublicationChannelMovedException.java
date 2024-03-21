package no.sikt.nva.pubchannels.dataporten;

import java.net.HttpURLConnection;
import java.net.URI;
import nva.commons.apigateway.exceptions.RedirectException;

public class PublicationChannelMovedException extends RedirectException {

    private final URI location;

    public PublicationChannelMovedException(String message, URI location) {
        super(message);
        this.location = location;
    }

    @Override
    public URI getLocation() {
        return location;
    }

    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_MOVED_PERM;
    }
}
