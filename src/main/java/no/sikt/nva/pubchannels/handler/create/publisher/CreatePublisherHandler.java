package no.sikt.nva.pubchannels.handler.create.publisher;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreatePublisherRequest;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.util.Map;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static nva.commons.core.attempt.Try.attempt;

public class CreatePublisherHandler extends CreateHandler<CreatePublisherRequest, Void> {

    private static final String PUBLISHER_PATH_ELEMENT = "publisher";

    @JacocoGenerated
    public CreatePublisherHandler() {
        super(CreatePublisherRequest.class, new Environment());
    }

    public CreatePublisherHandler(Environment environment, DataportenPublicationChannelClient client) {
        super(CreatePublisherRequest.class, environment, client);
    }

    @Override
    protected Void processInput(CreatePublisherRequest input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        var validInput = attempt(() -> validate(input))
                .map(CreatePublisherHandler::getClientRequest)
                .orElseThrow(failure -> new BadRequestException(failure.getException().getMessage()));
        var pid = publicationChannelClient.createPublisher(validInput);
        var createdUri = constructIdUri(PUBLISHER_PATH_ELEMENT, pid.getPid());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, createdUri.toString()));
        return null;
    }

    private static DataportenCreatePublisherRequest getClientRequest(CreatePublisherRequest request) {
        return new DataportenCreatePublisherRequest(
                request.getName(),
                request.getPrintIssn(),
                request.getOnlineIssn(),
                request.getHomepage());

    }

    private CreatePublisherRequest validate(CreatePublisherRequest input) {
        validateString(input.getName(), 5, 300, "Name");
        validateOptionalIssn(input.getPrintIssn(), "PrintIssn");
        validateOptionalIssn(input.getOnlineIssn(), "OnlineIssn");
        validateOptionalUrl(input.getHomepage(), "Homepage");
        return input;
    }

}
