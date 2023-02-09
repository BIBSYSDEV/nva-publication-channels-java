package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.handler.no.sikt.nva.pubchannels.handler.request.validator.FetchRequestValidator;
import no.sikt.nva.pubchannels.handler.no.sikt.nva.pubchannels.handler.request.validator.ValidationException;
import no.sikt.nva.pubchannels.handler.no.sikt.nva.pubchannels.handler.request.validator.Validator;
import nva.commons.apigateway.RequestInfo;

public final class FetchJournalRequest {
    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";
    private static final String YEAR_PATH_PARAM_NAME = "year";

    private final String identifier;
    private final String year;

    public FetchJournalRequest(RequestInfo requestInfo) throws ValidationException {
        this.identifier = requestInfo.getPathParameter(IDENTIFIER_PATH_PARAM_NAME).trim();
        this.year = requestInfo.getPathParameter(YEAR_PATH_PARAM_NAME).trim();
        validate(new FetchRequestValidator());
    }

    private void validate(Validator validator) throws ValidationException {
        validator.validate(this);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getYear() {
        return year;
    }
}
