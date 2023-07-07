package no.sikt.nva.pubchannels.handler.fetch.journal;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
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

        var request = attempt(() -> validate(requestInfo))
                          .map(FetchByIdAndYearRequest::new)
                          .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));

        var journalIdBaseUri = constructPublicationChannelIdBaseUri(JOURNAL_PATH_ELEMENT);

        var requestYear = request.getYear();
        return FetchByIdAndYearResponse.create(journalIdBaseUri,
                                               (ThirdPartyJournal) publicationChannelClient.getChannel(
                                                   ChannelType.JOURNAL,
                                                   request.getIdentifier(), requestYear), requestYear);
    }
}
