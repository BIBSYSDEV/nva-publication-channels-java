package no.sikt.nva.pubchannels.handler.search;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

public record SearchParameters(String query, int offset, int size, String year) {

    private static final int DEFAULT_QUERY_SIZE = 10;
    private static final int DEFAULT_OFFSET_SIZE = 0;
    private static final String QUERY_SIZE_PARAM = "size";
    private static final String QUERY_OFFSET_PARAM = "offset";
    private static final String YEAR_QUERY_PARAM = "year";
    private static final String QUERY_PARAM = "query";

    public static SearchParameters fromRequestInfo(RequestInfo requestInfo) throws BadRequestException {
        return new SearchParameters(
            requestInfo.getQueryParameter(QUERY_PARAM),
            requestInfo.getQueryParameterOpt(QUERY_OFFSET_PARAM).map(Integer::parseInt).orElse(DEFAULT_OFFSET_SIZE),
            requestInfo.getQueryParameterOpt(QUERY_SIZE_PARAM).map(Integer::parseInt).orElse(DEFAULT_QUERY_SIZE),
            requestInfo.getQueryParameter(YEAR_QUERY_PARAM)
        );
    }
}
