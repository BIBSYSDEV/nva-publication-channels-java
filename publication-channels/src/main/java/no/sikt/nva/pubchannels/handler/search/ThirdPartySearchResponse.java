package no.sikt.nva.pubchannels.handler.search;

import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;

public interface ThirdPartySearchResponse {

  ThirdPartyResultSet<? extends ThirdPartyPublicationChannel> resultSet();

  ThirdPartyPageInformation pageInformation();
}
