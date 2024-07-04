package no.sikt.nva.pubchannels.handler.fetch.journal;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelRegistry.ChannelType;
import no.sikt.nva.pubchannels.channelRegistry.PublicationChannelMovedException;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchJournalByIdentifierAndYearHandler extends
                                                    FetchByIdentifierAndYearHandler<Void, FetchByIdAndYearResponse> {

    private static final String JOURNAL_PATH_ELEMENT = "journal";

    @JacocoGenerated
    public FetchJournalByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchJournalByIdentifierAndYearHandler(Environment environment,
                                                  PublicationChannelClient publicationChannelClient) {
        super(Void.class, environment, publicationChannelClient);
    }

    @Override
    protected FetchByIdAndYearResponse processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var request = new FetchByIdAndYearRequest(requestInfo);
        var journalIdBaseUri = constructPublicationChannelIdBaseUri(JOURNAL_PATH_ELEMENT);

        var requestYear = request.getYear();
        var journal = fetchJournal(request, requestYear);
        return FetchByIdAndYearResponse.create(journalIdBaseUri, (ThirdPartyJournal) journal, requestYear);
    }

    private ThirdPartyPublicationChannel fetchJournal(FetchByIdAndYearRequest request, String requestYear)
        throws ApiGatewayException {
        try {
            return publicationChannelClient.getChannel(ChannelType.JOURNAL, request.getIdentifier(), requestYear);
        } catch (PublicationChannelMovedException movedException) {
            throw new PublicationChannelMovedException(
                "Journal moved", constructNewLocation(JOURNAL_PATH_ELEMENT, movedException.getLocation(), requestYear));
        }
    }
}
