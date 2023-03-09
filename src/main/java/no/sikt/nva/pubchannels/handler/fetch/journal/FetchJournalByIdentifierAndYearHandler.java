package no.sikt.nva.pubchannels.handler.fetch.journal;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import static nva.commons.core.attempt.Try.attempt;

public class FetchJournalByIdentifierAndYearHandler extends
        FetchByIdentifierAndYearHandler<Void, FetchByIdAndYearResponse> {

    public static final String JOURNAL_PATH_ELEMENT = "journal";

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

        return FetchByIdAndYearResponse.create(journalIdBaseUri,
                publicationChannelClient.getJournal(request.getIdentifier(), request.getYear()));
    }

}
