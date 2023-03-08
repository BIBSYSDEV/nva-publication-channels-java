package no.sikt.nva.pubchannels.handler.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.time.Year;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateYear;
import static nva.commons.core.attempt.Try.attempt;

public class FetchJournalByQueryHandler extends ApiGatewayHandler<Void, Void> {

    public static final String YEAR_QUERY_PARAM = "year";

    @JacocoGenerated
    public FetchJournalByQueryHandler() {
        super(Void.class, new Environment());
    }

    public FetchJournalByQueryHandler(Environment environment) {
        super(Void.class, environment);
    }

    @Override
    protected Void processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        attempt(() -> validate(requestInfo))
                .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));
        return null;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Void output) {
        return null;
    }

    private RequestInfo validate(RequestInfo requestInfo) throws BadRequestException {
        validateYear(requestInfo.getQueryParameter(YEAR_QUERY_PARAM), Year.of(Year.MIN_VALUE), "Year");
        requestInfo.getQueryParameterOpt("name").ifPresent(name -> validateString(name, 5, 300, "Name"));
        requestInfo.getQueryParameterOpt("issn").ifPresent(issn -> validateOptionalIssn(issn, "Issn"));
        return requestInfo;
    }
}
