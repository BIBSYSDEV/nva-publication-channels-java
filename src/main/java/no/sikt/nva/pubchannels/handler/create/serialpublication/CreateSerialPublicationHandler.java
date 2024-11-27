package no.sikt.nva.pubchannels.handler.create.serialpublication;

import static java.util.Objects.requireNonNull;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.handler.validator.ValidationException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

public class CreateSerialPublicationHandler
    extends CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> {

    public CreateSerialPublicationHandler(Environment environment, ChannelRegistryClient channelRegistryClient) {
        super(CreateSerialPublicationRequest.class, environment, channelRegistryClient);
    }

    @Override
    protected void validateRequest(CreateSerialPublicationRequest createSerialPublicationRequest,
                                   RequestInfo requestInfo, Context context) throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        validate(createSerialPublicationRequest);
    }

    @Override
    protected SerialPublicationDto processInput(CreateSerialPublicationRequest createSerialPublicationRequest,
                                                RequestInfo requestInfo, Context context) throws ApiGatewayException {
        return null;
    }

    private void validate(CreateSerialPublicationRequest input) throws BadRequestException {
        try {
            validateString(input.name(), 5, 300, "Name");
            validateType(input.type());
            validateOptionalIssn(input.printIssn(), "PrintIssn");
            validateOptionalIssn(input.onlineIssn(), "OnlineIssn");
            validateOptionalUrl(input.homepage(), "Homepage");
        } catch (ValidationException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }

    private void validateType(String type) {
        requireNonNull(type);
        var typeLowerCase = type.toLowerCase();
        if (!"journal".equals(typeLowerCase) && !"series".equals(typeLowerCase)) {
            throw new ValidationException("Type must be either 'Journal' or 'Series'");
        }
    }
}
