package no.sikt.nva.pubchannels.handler.create.publisher;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.PUBLISHER;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIsbnPrefix;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreatePublisherRequest;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.validator.ValidationException;
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

    public CreatePublisherHandler(Environment environment, ChannelRegistryClient client) {
        super(CreatePublisherRequest.class, environment, client);
    }

    @Override
    protected void validateRequest(CreatePublisherRequest createPublisherRequest, RequestInfo requestInfo,
                                   Context context) throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        validate(createPublisherRequest);
    }

    @Override
    protected CreatePublisherResponse processInput(CreatePublisherRequest input, RequestInfo requestInfo,
                                                   Context context) throws ApiGatewayException {
        var response = publicationChannelClient.createPublisher(getClientRequest(input));
        var createdUri = constructIdUri(PUBLISHER_PATH_ELEMENT, response.pid());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, createdUri.toString()));
        return CreatePublisherResponse.create(
            createdUri,
            (ThirdPartyPublisher) publicationChannelClient.getChannel(PUBLISHER, response.pid(), getYear()));
    }

    private static ChannelRegistryCreatePublisherRequest getClientRequest(CreatePublisherRequest request) {
        return new ChannelRegistryCreatePublisherRequest(request.name(), request.isbnPrefix(), request.homepage());
    }

    private void validate(CreatePublisherRequest input) throws BadRequestException {
        try {
            validateString(input.name(), 5, 300, "Name");
            validateOptionalIsbnPrefix(input.isbnPrefix(), "Isbn prefix");
            validateOptionalUrl(input.homepage(), "Homepage");
        } catch (ValidationException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }
}
