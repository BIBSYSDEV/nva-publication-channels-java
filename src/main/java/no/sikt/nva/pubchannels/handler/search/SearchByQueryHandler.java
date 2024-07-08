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
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
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
    private static final String ISSN_QUERY_PARAM = "issn";
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
        this.publicationChannelClient = ChannelRegistryClient.defaultInstance();
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
        validate(requestInfo);
    }

    @Override
    protected PaginatedSearchResult<T> processInput(Void input, RequestInfo requestInfo,
                                                    Context context) throws ApiGatewayException {
        var searchParameters = SearchParameters.fromRequestInfo(requestInfo);
        var searchResult = searchChannel(searchParameters);

        return PaginatedSearchResult.create(
            constructBaseUri(),
            searchParameters.offset(),
            searchParameters.size(),
            searchResult.pageInformation().totalResults(),
            getHits(constructBaseUri(), searchResult, searchParameters.year()),
            Map.of(QUERY_PARAM, searchParameters.query(), YEAR_QUERY_PARAM, searchParameters.year())
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

    private void validate(RequestInfo requestInfo) throws BadRequestException {
        attempt(() -> {
            var parameters = SearchParameters.fromRequestInfo(requestInfo);
            validateYear(parameters.year(), Year.of(1900), "Year");
            validateString(parameters.query(), 4, 300, "Query");
            validatePagination(parameters.offset(), parameters.size());
            return null;
        }).orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));
    }

    private ThirdPartySearchResponse searchChannel(SearchParameters searchParameters)
        throws ApiGatewayException {
        var queryParams = getQueryParams(searchParameters);
        return publicationChannelClient.searchChannel(channelType, queryParams);
    }

    private List<T> getHits(URI baseUri, ThirdPartySearchResponse searchResult, String requestedYear) {
        return searchResult.resultSet()
                   .pageResult()
                   .stream()
                   .map(result -> createResult(baseUri, result, requestedYear))
                   .collect(Collectors.toList());
    }

    private Map<String, String> getQueryParams(SearchParameters parameters) {
        var queryParams = new HashMap<String, String>();
        queryParams.put(YEAR_QUERY_PARAM, parameters.year());

        var query = parameters.query();
        if (isQueryParameterIssn(query)) {
            queryParams.put(ISSN_QUERY_PARAM, query.trim());
        } else {
            queryParams.put(NAME_QUERY_PARAM, query.trim());
        }

        queryParams.put(PAGENO_QUERY_PARAM, String.valueOf(parameters.offset() / parameters.size()));
        queryParams.put(PAGECOUNT_QUERY_PARAM, String.valueOf(parameters.size()));
        return queryParams;
    }

    private boolean isQueryParameterIssn(String query) {
        return ISSNValidator.getInstance().isValid(query.trim());
    }
}
