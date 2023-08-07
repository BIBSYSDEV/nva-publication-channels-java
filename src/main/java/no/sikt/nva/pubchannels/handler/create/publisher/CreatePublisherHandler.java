package no.sikt.nva.pubchannels.handler.create.publisher;

import static no.sikt.nva.pubchannels.dataporten.ChannelType.PUBLISHER;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIsbnPrefix;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreatePublisherRequest;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreatePublisherHandler extends CreateHandler<CreatePublisherRequest, CreatePublisherResponse> {

    private static final String PUBLISHER_PATH_ELEMENT = "publisher";

    @JacocoGenerated
    public CreatePublisherHandler() {
        super(CreatePublisherRequest.class, new Environment());
    }

    public CreatePublisherHandler(Environment environment, DataportenPublicationChannelClient client) {
        super(CreatePublisherRequest.class, environment, client);
    }

    @Override
    protected CreatePublisherResponse processInput(CreatePublisherRequest input, RequestInfo requestInfo,
                                                    Context context) throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        var validInput = attempt(() -> validate(input))
                             .map(CreatePublisherHandler::getClientRequest)
                             .orElseThrow(failure -> new BadRequestException(failure.getException().getMessage()));
        var createResponse = publicationChannelClient.createPublisher(validInput);
        var createdUri = constructIdUri(PUBLISHER_PATH_ELEMENT, createResponse.getPid());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, createdUri.toString()));
        return CreatePublisherResponse.create(
            createdUri,
            (ThirdPartyPublisher) publicationChannelClient.getChannel(PUBLISHER, createResponse.getPid(), getYear()),
            getYear());
    }

    private static DataportenCreatePublisherRequest getClientRequest(CreatePublisherRequest request) {
        return new DataportenCreatePublisherRequest(request.getName(), request.getIsbnPrefix(), request.getHomepage());
    }

    private CreatePublisherRequest validate(CreatePublisherRequest input) {
        validateString(input.getName(), 5, 300, "Name");
        validateOptionalIsbnPrefix(input.getIsbnPrefix(), "Isbn prefix");
        validateOptionalUrl(input.getHomepage(), "Homepage");
        return input;
    }
}
