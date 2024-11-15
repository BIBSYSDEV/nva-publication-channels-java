package no.sikt.nva.pubchannels.channelregistrycache;

import nva.commons.apigateway.exceptions.NotFoundException;

public class CachedPublicationChannelNotFoundException extends NotFoundException {

    public CachedPublicationChannelNotFoundException(String message) {
        super(message);
    }

}
