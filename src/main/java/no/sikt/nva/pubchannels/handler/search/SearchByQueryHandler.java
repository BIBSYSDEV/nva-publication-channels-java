package no.sikt.nva.pubchannels.handler.search;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validatePagination;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateYear;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.apache.commons.validator.routines.ISSNValidator;

public abstract class SearchByQueryHandler<T> extends ApiGatewayHandler<Void, PaginatedSearchResult<T>> {

    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String QUERY_SIZE_PARAM = "size";
    private static final String QUERY_OFFSET_PARAM = "offset";
    private static final String ISSN_QUERY_PARAM = "issn";
    private static final int DEFAULT_QUERY_SIZE = 10;
    private static final int DEFAULT_OFFSET_SIZE = 0;
    private static final String YEAR_QUERY_PARAM = "year";
    private static final String QUERY_PARAM = "query";
    private static final String NAME_QUERY_PARAM = "name";
    private static final String PAGENO_QUERY_PARAM = "pageno";
    private static final String PAGECOUNT_QUERY_PARAM = "pagecount";
    private final String pathElement;
    private final PublicationChannelClient publicationChannelClient;
    private final ChannelType channelType;

    @JacocoGenerated
    protected SearchByQueryHandler(String pathElement, ChannelType channelType) {
        super(Void.class, new Environment());
        this.publicationChannelClient = DataportenPublicationChannelClient.defaultInstance();
        this.pathElement = pathElement;
        this.channelType = channelType;
    }

    protected SearchByQueryHandler(Environment environment, PublicationChannelClient publicationChannelClient,
                                   String pathElement, ChannelType channelType) {
        super(Void.class, environment);
        this.publicationChannelClient = publicationChannelClient;
        this.pathElement = pathElement;
        this.channelType = channelType;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(
            JSON_UTF_8,
            APPLICATION_JSON_LD
        );
    }

    @Override
    protected void validateRequest(Void unused, RequestInfo requestInfo, Context context) throws ApiGatewayException {

    }

    @Override
    protected PaginatedSearchResult<T> processInput(Void input, RequestInfo requestInfo,
                                                    Context context) throws ApiGatewayException {
        var year = requestInfo.getQueryParameter(YEAR_QUERY_PARAM);
        var query = requestInfo.getQueryParameter(QUERY_PARAM);
        int offset = requestInfo.getQueryParameterOpt(QUERY_OFFSET_PARAM).map(Integer::parseInt)
                         .orElse(DEFAULT_OFFSET_SIZE);
        int size = requestInfo.getQueryParameterOpt(QUERY_SIZE_PARAM).map(Integer::parseInt).orElse(DEFAULT_QUERY_SIZE);

        validate(year, query, offset, size);

        var searchResult = searchChannel(year, query, offset, size);

        return PaginatedSearchResult.create(
            constructBaseUri(),
            offset,
            size,
            searchResult.getPageInformation().getTotalResults(),
            getHits(constructBaseUri(), searchResult, year),
            Map.of(QUERY_PARAM, query, YEAR_QUERY_PARAM, year)
        );
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, PaginatedSearchResult<T> output) {
        return HttpURLConnection.HTTP_OK;
    }

    protected abstract T createResult(URI baseUri, ThirdPartyPublicationChannel entityResult, String requestedYear);

    protected URI constructBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                   .addChild(customDomainBasePath, pathElement)
                   .getUri();
    }

    private void validate(String year, String query, int offset, int size) throws BadRequestException {
        attempt(() -> {
            validateYear(year, Year.of(1900), "Year");
            validateString(query, 4, 300, "Query");
            validatePagination(offset, size);
            return null;
        }).orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));
    }

    private ThirdPartySearchResponse searchChannel(String year, String query, int offset, int size)
        throws ApiGatewayException {
        var queryParams = getQueryParams(year, query, offset, size);
        return publicationChannelClient.searchChannel(channelType, queryParams);
    }

    private List<T> getHits(URI baseUri, ThirdPartySearchResponse searchResult, String requestedYear) {
        return searchResult.getResultSet()
                   .getPageResult()
                   .stream()
                   .map(result -> createResult(baseUri, result, requestedYear))
                   .collect(Collectors.toList());
    }

    private Map<String, String> getQueryParams(String year, String query, int offset, int size) {
        var queryParams = new HashMap<String, String>();
        queryParams.put(YEAR_QUERY_PARAM, year);

        if (isQueryParameterIssn(query)) {
            queryParams.put(ISSN_QUERY_PARAM, query.trim());
        } else {
            queryParams.put(NAME_QUERY_PARAM, query.trim());
        }

        queryParams.put(PAGENO_QUERY_PARAM, String.valueOf(offset / size));
        queryParams.put(PAGECOUNT_QUERY_PARAM, String.valueOf(size));
        return queryParams;
    }

    private boolean isQueryParameterIssn(String query) {
        return ISSNValidator.getInstance().isValid(query.trim());
    }
}
