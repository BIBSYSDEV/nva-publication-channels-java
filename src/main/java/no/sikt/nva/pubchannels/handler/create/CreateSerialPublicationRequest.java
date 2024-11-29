package no.sikt.nva.pubchannels.handler.create;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import no.sikt.nva.pubchannels.handler.validator.ValidationException;
import nva.commons.apigateway.exceptions.BadRequestException;

public record CreateSerialPublicationRequest(String name,
                                             String printIssn,
                                             String onlineIssn,
                                             String homepage,
                                             String type) {

    public void validate() throws BadRequestException {
        try {
            validateString(name, 5, 300, "Name");
            validateOptionalIssn(printIssn, "PrintIssn");
            validateOptionalIssn(onlineIssn, "OnlineIssn");
            validateOptionalUrl(homepage, "Homepage");
        } catch (ValidationException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }
}
