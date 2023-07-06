package no.sikt.nva.pubchannels.handler.create.journal;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreateJournalRequest;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreateJournalHandler extends CreateHandler<CreateJournalRequest, Void> {

    private static final String JOURNAL_PATH_ELEMENT = "journal";

    @JacocoGenerated
    public CreateJournalHandler() {
        super(CreateJournalRequest.class, new Environment());
    }

    public CreateJournalHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(CreateJournalRequest.class, environment, publicationChannelClient);
    }

    @Override
    protected Void processInput(CreateJournalRequest input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        var validInput = attempt(() -> validate(input))
                             .map(CreateJournalHandler::getClientRequest)
                             .orElseThrow(failure -> new BadRequestException(failure.getException().getMessage()));

        var journalPid = publicationChannelClient.createJournal(validInput);
        var createdUri = constructIdUri(JOURNAL_PATH_ELEMENT, journalPid.getPid());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, createdUri.toString()));
        return null;
    }

    private static DataportenCreateJournalRequest getClientRequest(CreateJournalRequest request) {
        return new DataportenCreateJournalRequest(
            request.getName(),
            request.getPrintIssn(),
            request.getOnlineIssn(),
            request.getHomepage());
    }

    private CreateJournalRequest validate(CreateJournalRequest input) {
        validateString(input.getName(), 5, 300, "Name");
        validateOptionalIssn(input.getPrintIssn(), "PrintIssn");
        validateOptionalIssn(input.getOnlineIssn(), "OnlineIssn");
        validateOptionalUrl(input.getHomepage(), "Homepage");
        return input;
    }
}
