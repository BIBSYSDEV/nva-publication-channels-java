package no.sikt.nva.pubchannels.handler.search.journal;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.search.DataportenFindJournalResponse;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.search.PagedSearchResult;
import no.sikt.nva.pubchannels.model.Contexts;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateYear;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;

public class SearchJournalByQueryHandler extends ApiGatewayHandler<Void, PagedSearchResult<JournalResult>> {


    public static final String PID_QUERY_PARAM = "pid";
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
    protected PagedSearchResult<JournalResult> processInput(Void input, RequestInfo requestInfo,
                                                            Context context) throws ApiGatewayException {

        attempt(() -> validate(requestInfo))
                .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));

        var year = requestInfo.getQueryParameter(YEAR_QUERY_PARAM);
        var query = requestInfo.getQueryParameter(QUERY_PARAM);

        var searchResult = searchJournal(year, query);

        return PagedSearchResult.create(
                URI.create(Contexts.PUBLICATION_CHANNEL_CONTEXT),
                constructJournalIdBaseUri(),
                requestInfo.getQueryParameterOpt(QUERY_OFFSET_PARAM).map(Integer::parseInt).orElse(DEFAULT_OFFSET_SIZE),
                requestInfo.getQueryParameterOpt(QUERY_SIZE_PARAM).map(Integer::parseInt).orElse(DEFAULT_QUERY_SIZE),
                searchResult.getPageInformation().getTotalResults(),
                getJournalHits(constructJournalIdBaseUri(), searchResult),
                Map.of(QUERY_PARAM, query, YEAR_QUERY_PARAM, year)
        );
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, PagedSearchResult<JournalResult> output) {
        return HttpURLConnection.HTTP_OK;
    }

    private DataportenFindJournalResponse searchJournal(String year, String query) throws ApiGatewayException {
        var queryParams = getQueryParams(year, query);

        return publicationChannelClient.getChannel(ChannelType.JOURNAL, queryParams);
    }

    private List<JournalResult> getJournalHits(URI baseUri, DataportenFindJournalResponse searchResult) {
        List<JournalResult> journalHits = new ArrayList<>();
        searchResult.getResultSet().getPageResult().forEach(result ->
                journalHits.add(JournalResult.create(baseUri, result)));
        return journalHits;
    }

    private Map<String, String> getQueryParams(String year, String query) {
        var queryParams = new HashMap<String, String>();
        queryParams.put(YEAR_QUERY_PARAM, year);

        if (isQueryParameterIssn(query)) {
            queryParams.put(ISSN_QUERY_PARAM, query.trim());
        } else if (isQueryParameterUuid(query)) {
            queryParams.put(PID_QUERY_PARAM, query.trim());
        }
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

    private RequestInfo validate(RequestInfo requestInfo) throws BadRequestException {
        validateYear(requestInfo.getQueryParameter(YEAR_QUERY_PARAM), Year.of(Year.MIN_VALUE), "Year");
        validateString(requestInfo.getQueryParameter(QUERY_PARAM), 0, 300, "Query");
        return requestInfo;
    }

    protected URI constructJournalIdBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, SearchJournalByQueryHandler.JOURNAL_PATH_ELEMENT)
                .getUri();
    }
}
