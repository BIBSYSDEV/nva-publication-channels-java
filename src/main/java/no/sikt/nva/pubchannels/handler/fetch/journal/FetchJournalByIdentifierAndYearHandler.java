package no.sikt.nva.pubchannels.handler.fetch.journal;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.function.Function;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.PublicationChannelMovedException;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.JournalDto;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchJournalByIdentifierAndYearHandler extends FetchByIdentifierAndYearHandler<Void, JournalDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchJournalByIdentifierAndYearHandler.class);
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

        var requestYear = request.getYear();

        var journal = shouldUseCache ? fetchJournalFromCache(request) : fetchJournalOrFetchFromCache(request);
        return JournalDto.create(journalIdBaseUri, (ThirdPartyJournal) journal, requestYear);
    }

    private ThirdPartyPublicationChannel fetchJournalOrFetchFromCache(FetchByIdAndYearRequest request)
        throws ApiGatewayException {
        try {
            return fetchJournal(request, request.getYear());
        } catch (ApiGatewayException e) {
            return fetchFromCacheWhenServerError(request, e);
        }
    }

    private ThirdPartyPublicationChannel fetchFromCacheWhenServerError(FetchByIdAndYearRequest request,
                                                                       ApiGatewayException e)
        throws ApiGatewayException {
        if (e.getStatusCode() >= 500) {
            return attempt(() -> fetchJournalFromCache(request)).orElseThrow(throwOriginalException(e));
        } else {
            throw e;
        }
    }

    private static Function<Failure<ThirdPartyPublicationChannel>, ApiGatewayException> throwOriginalException(
        ApiGatewayException e) {
        return failure -> e;
    }

    private ThirdPartyPublicationChannel fetchJournalFromCache(FetchByIdAndYearRequest request)
        throws ApiGatewayException {
        LOGGER.info("Fetching journal from cache: {}", request.getIdentifier());
        return cacheService.getChannel(ChannelType.JOURNAL, request.getIdentifier(), request.getYear());
    }

    private ThirdPartyPublicationChannel fetchJournal(FetchByIdAndYearRequest request, String requestYear)
        throws ApiGatewayException {
        try {
            LOGGER.info("Fetching journal from channel register: {}", request.getIdentifier());
            return publicationChannelClient.getChannel(ChannelType.JOURNAL, request.getIdentifier(), requestYear);
        } catch (PublicationChannelMovedException movedException) {
            throw new PublicationChannelMovedException("Journal moved",
                                                       constructNewLocation(JOURNAL_PATH_ELEMENT,
                                                                            movedException.getLocation(),
                                                                            requestYear));
        }
    }
}
