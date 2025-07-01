package no.sikt.nva.pubchannels.channelregistrycache;

import nva.commons.apigateway.exceptions.NotFoundException;

public class CachedPublicationChannelNotFoundException extends NotFoundException {

  public static final String CHANNEL_NOT_FOUND_MESSAGE =
      "Could not find cached publication channel with identifier %s";

  public CachedPublicationChannelNotFoundException(String identifier) {
    super(CHANNEL_NOT_FOUND_MESSAGE.formatted(identifier));
  }
}
