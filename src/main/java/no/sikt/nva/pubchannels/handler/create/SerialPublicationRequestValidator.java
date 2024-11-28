package no.sikt.nva.pubchannels.handler.create;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import no.sikt.nva.pubchannels.handler.validator.ValidationException;
import nva.commons.apigateway.exceptions.BadRequestException;

public final class SerialPublicationRequestValidator {

    private SerialPublicationRequestValidator() {
        // NO-OP
    }

    public static void validateRequest(CreateSerialPublicationRequest request) throws BadRequestException {
        try {
            validateString(request.name(), 5, 300, "Name");
            validateOptionalIssn(request.printIssn(), "PrintIssn");
            validateOptionalIssn(request.onlineIssn(), "OnlineIssn");
            validateOptionalUrl(request.homepage(), "Homepage");
        } catch (ValidationException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }
}
