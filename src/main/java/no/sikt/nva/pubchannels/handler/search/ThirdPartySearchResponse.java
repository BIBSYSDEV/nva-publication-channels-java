package no.sikt.nva.pubchannels.handler.search;

import no.sikt.nva.pubchannels.handler.fetch.ThirdPartyPublicationChannel;

public interface ThirdPartySearchResponse {

    ThirdPartyResultSet<? extends ThirdPartyPublicationChannel> getResultSet();

    ThirdPartyPageInformation getPageInformation();
}
