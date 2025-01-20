package no.sikt.nva.pubchannels.handler.fetch;

import nva.commons.apigateway.RequestInfo;

public final class FetchByIdAndYearRequest {

    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";
    private static final String YEAR_PATH_PARAM_NAME = "year";

    private final String identifier;
    private final String year;

    public FetchByIdAndYearRequest(RequestInfo requestInfo) {
        this.identifier = requestInfo
                              .getPathParameter(IDENTIFIER_PATH_PARAM_NAME)
                              .trim();
        this.year = requestInfo
                        .getPathParameter(YEAR_PATH_PARAM_NAME)
                        .trim();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getYear() {
        return year;
    }
}
