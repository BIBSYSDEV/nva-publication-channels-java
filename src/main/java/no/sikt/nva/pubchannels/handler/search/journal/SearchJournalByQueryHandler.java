package no.sikt.nva.pubchannels.handler.search.journal;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.search.DataportenSearchJournalResponse;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.apache.commons.validator.routines.ISSNValidator;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validatePagination;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateYear;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;

public class SearchJournalByQueryHandler extends ApiGatewayHandler<Void, PaginatedSearchResult<JournalResult>> {


    private static final String PID_QUERY_PARAM = "pid";
    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String JOURNAL_PATH_ELEMENT = "journal";
    private static final String QUERY_SIZE_PARAM = "size";
    private static final String QUERY_OFFSET_PARAM = "offset";
    private static final String ISSN_QUERY_PARAM = "issn";
    private static final int DEFAULT_QUERY_SIZE = 10;
    private static final int DEFAULT_OFFSET_SIZE = 0;
    private static final String YEAR_QUERY_PARAM = "year";
    private static final String QUERY_PARAM = "query";
    private static final String NAME_QUERY_PARAM = "name";
    private final PublicationChannelClient publicationChannelClient;

    @JacocoGenerated
    public SearchJournalByQueryHandler() {
        super(Void.class, new Environment());
        this.publicationChannelClient = DataportenPublicationChannelClient.defaultInstance();
    }

    public SearchJournalByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(Void.class, environment);
        this.publicationChannelClient = publicationChannelClient;
    }

    @Override
    protected PaginatedSearchResult<JournalResult> processInput(Void input, RequestInfo requestInfo,
                                                                Context context) throws ApiGatewayException {
        var year = requestInfo.getQueryParameter(YEAR_QUERY_PARAM);
        var query = requestInfo.getQueryParameter(QUERY_PARAM);
        int offset = requestInfo.getQueryParameterOpt(QUERY_OFFSET_PARAM).map(Integer::parseInt)
                .orElse(DEFAULT_OFFSET_SIZE);
        int size = requestInfo.getQueryParameterOpt(QUERY_SIZE_PARAM).map(Integer::parseInt).orElse(DEFAULT_QUERY_SIZE);

        validate(year, query, offset, size);

        var searchResult = searchJournal(year, query, offset, size);

        return PaginatedSearchResult.create(
                constructJournalIdBaseUri(),
                offset,
                size,
                searchResult.getPageInformation().getTotalResults(),
                getJournalHits(constructJournalIdBaseUri(), searchResult),
                Map.of(QUERY_PARAM, query, YEAR_QUERY_PARAM, year)
        );
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, PaginatedSearchResult<JournalResult> output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validate(String year, String query, int offset, int size) throws BadRequestException {
        attempt(() -> {
            validateYear(year, Year.of(1900), "Year");
            validateString(query, 0, 300, "Query");
            validatePagination(offset, size);
            return null;
        })
                .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));
    }

    private DataportenSearchJournalResponse searchJournal(String year, String query, int offset, int size)
            throws ApiGatewayException {
        var queryParams = getQueryParams(year, query, offset, size);
        return publicationChannelClient.getChannel(ChannelType.JOURNAL, queryParams);
    }

    private List<JournalResult> getJournalHits(URI baseUri, DataportenSearchJournalResponse searchResult) {
        return searchResult.getResultSet()
                .getPageResult()
                .stream()
                .map(result -> JournalResult.create(baseUri, result))
                .collect(Collectors.toList());
    }

    private Map<String, String> getQueryParams(String year, String query, int offset, int size) {
        var queryParams = new HashMap<String, String>();
        queryParams.put(YEAR_QUERY_PARAM, year);

        if (isQueryParameterIssn(query)) {
            queryParams.put(ISSN_QUERY_PARAM, query.trim());
        } else if (isQueryParameterUuid(query)) {
            queryParams.put(PID_QUERY_PARAM, query.trim());
        } else {
            queryParams.put(NAME_QUERY_PARAM, query.trim());
        }

        queryParams.put("pageno", String.valueOf(offset / size));
        queryParams.put("pagecount", String.valueOf(size));
        return queryParams;
    }

    private boolean isQueryParameterUuid(String query) {
        try {
            UUID.fromString(query.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isQueryParameterIssn(String query) {
        return ISSNValidator.getInstance().isValid(query.trim());
    }

    protected URI constructJournalIdBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, SearchJournalByQueryHandler.JOURNAL_PATH_ELEMENT)
                .getUri();
    }
}
