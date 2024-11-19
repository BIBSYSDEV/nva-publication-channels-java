package no.sikt.nva.pubchannels.handler.fetch.journal;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.JOURNAL;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdAndYearRequest;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.JournalDto;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchJournalByIdentifierAndYearHandler extends FetchByIdentifierAndYearHandler<Void, JournalDto> {

    private static final String JOURNAL_PATH_ELEMENT = "journal";

    @JacocoGenerated
    public FetchJournalByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchJournalByIdentifierAndYearHandler(Environment environment,
                                                  PublicationChannelClient publicationChannelClient,
                                                  CacheService cacheService) {
        super(Void.class, environment, publicationChannelClient, cacheService);
    }

    @Override
    protected JournalDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        var request = new FetchByIdAndYearRequest(requestInfo);
        var journalIdBaseUri = constructPublicationChannelIdBaseUri(JOURNAL_PATH_ELEMENT);

        var identifier = request.getIdentifier();
        var year = request.getYear();

        var journal = super.shouldUseCache()
                          ? super.fetchChannelFromCache(JOURNAL, identifier, year)
                          : super.fetchChannelOrFetchFromCache(JOURNAL, identifier, year);
        return JournalDto.create(journalIdBaseUri, (ThirdPartySerialPublication) journal, year);
    }
}
