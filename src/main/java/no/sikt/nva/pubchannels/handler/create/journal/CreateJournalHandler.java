package no.sikt.nva.pubchannels.handler.create.journal;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.JOURNAL;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateJournalRequest;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyJournal;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.validator.ValidationException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreateJournalHandler extends CreateHandler<CreateJournalRequest, CreateJournalResponse> {

    private static final String JOURNAL_PATH_ELEMENT = "journal";

    @JacocoGenerated
    public CreateJournalHandler() {
        super(CreateJournalRequest.class, new Environment());
    }

    public CreateJournalHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(CreateJournalRequest.class, environment, publicationChannelClient);
    }

    @Override
    protected void validateRequest(CreateJournalRequest createJournalRequest, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        validate(createJournalRequest);
    }

    @Override
    protected CreateJournalResponse processInput(CreateJournalRequest input, RequestInfo requestInfo,
                                                 Context context) throws ApiGatewayException {
        var response = publicationChannelClient.createJournal(getClientRequest(input));
        var createdUri = constructIdUri(JOURNAL_PATH_ELEMENT, response.pid());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, createdUri.toString()));
        return CreateJournalResponse.create(
            createdUri,
            (ThirdPartyJournal) publicationChannelClient.getChannel(JOURNAL, response.pid(), getYear()));
    }

    private static ChannelRegistryCreateJournalRequest getClientRequest(CreateJournalRequest request) {
        return new ChannelRegistryCreateJournalRequest(request.name(), request.printIssn(), request.onlineIssn(),
                                                       request.homepage());
    }

    private void validate(CreateJournalRequest input) throws BadRequestException {
        try {
            validateString(input.name(), 5, 300, "Name");
            validateOptionalIssn(input.printIssn(), "PrintIssn");
            validateOptionalIssn(input.onlineIssn(), "OnlineIssn");
            validateOptionalUrl(input.homepage(), "Homepage");
        } catch (ValidationException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }
}
