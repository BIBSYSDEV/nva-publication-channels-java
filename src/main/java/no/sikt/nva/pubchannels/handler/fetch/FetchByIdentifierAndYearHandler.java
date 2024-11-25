package no.sikt.nva.pubchannels.handler.fetch;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateUuid;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateYear;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Year;
import java.util.List;
import java.util.function.Function;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.ChannelType;
import no.sikt.nva.pubchannels.channelregistry.PublicationChannelMovedException;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.utils.AppConfig;
import no.sikt.nva.pubchannels.utils.ApplicationConfiguration;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FetchByIdentifierAndYearHandler<I, O> extends ApiGatewayHandler<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchByIdentifierAndYearHandler.class);
    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String YEAR_PATH_PARAM_NAME = "year";
    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";
    private static final String YEAR = "Year";
    private static final String PID = "Pid";
    private static final String FETCHING_FROM_CACHE_MESSAGE = "Fetching {} from cache: {}";
    private static final String FETCHING_FROM_CHANNEL_REGISTER_MESSAGE = "Fetching {} from channel register: {}";

    protected final PublicationChannelClient publicationChannelClient;
    protected final CacheService cacheService;
    protected final AppConfig appConfig;

    @JacocoGenerated
    protected FetchByIdentifierAndYearHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
        this.publicationChannelClient = ChannelRegistryClient.defaultInstance();
        this.cacheService = CacheService.defaultInstance();
        this.appConfig = ApplicationConfiguration.defaultAppConfigClientInstance();
    }

    protected FetchByIdentifierAndYearHandler(Class<I> requestClass, Environment environment,
                                              PublicationChannelClient client, CacheService cacheService,
                                              AppConfig appConfig) {
        super(requestClass, environment);
        this.publicationChannelClient = client;
        this.cacheService = cacheService;
        this.appConfig = appConfig;
    }

    protected RequestInfo validate(RequestInfo requestInfo) {
        validateUuid(requestInfo.getPathParameter(IDENTIFIER_PATH_PARAM_NAME).trim(), PID);
        validateYear(requestInfo.getPathParameter(YEAR_PATH_PARAM_NAME).trim(), Year.of(Year.MIN_VALUE),
                     YEAR);
        return requestInfo;
    }

    protected ThirdPartyPublicationChannel fetchChannelFromChannelRegister(ChannelType type, String identifier,
                                                                           String year)
        throws ApiGatewayException {
        try {
            LOGGER.info(FETCHING_FROM_CHANNEL_REGISTER_MESSAGE, type.name(), identifier);
            return publicationChannelClient.getChannel(type, identifier, year);
        } catch (PublicationChannelMovedException movedException) {
            throw new PublicationChannelMovedException(
                "%s moved".formatted(type.name()),
                constructNewLocation(getPathElement(), movedException.getLocation(), year));
        }
    }

    protected boolean shouldUseCache() {
        return appConfig.shouldUseCache();
    }

    protected ThirdPartyPublicationChannel fetchFromCacheWhenServerError(ChannelType type, String identifier,
                                                                         String year,
                                                                         ApiGatewayException e)
        throws ApiGatewayException {
        if (isServerError(e)) {
            return attempt(() -> fetchChannelFromCache(type, identifier, year)).orElseThrow(throwOriginalException(e));
        } else {
            throw e;
        }
    }

    protected ThirdPartyPublicationChannel fetchChannelOrFetchFromCache(ChannelType type, String identifier,
                                                                        String year)
        throws ApiGatewayException {
        try {
            return fetchChannelFromChannelRegister(type, identifier, year);
        } catch (ApiGatewayException e) {
            return fetchFromCacheWhenServerError(type, identifier, year, e);
        }
    }

    protected ThirdPartyPublicationChannel fetchChannelFromCache(ChannelType type, String identifier, String year)
        throws ApiGatewayException {
        LOGGER.info(FETCHING_FROM_CACHE_MESSAGE, type.name(), identifier);
        return cacheService.getChannel(type, identifier, year);
    }

    protected URI constructPublicationChannelIdBaseUri(String publicationChannelPathElement) {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                   .addChild(customDomainBasePath, publicationChannelPathElement)
                   .getUri();
    }

    protected URI constructNewLocation(String pathElement, URI channelRegistryLocation, String year) {
        var newIdentifier = UriWrapper.fromUri(channelRegistryLocation).getPath().getPathElementByIndexFromEnd(1);
        return UriWrapper.fromUri(constructPublicationChannelIdBaseUri(pathElement))
                   .addChild(newIdentifier)
                   .addChild(year)
                   .getUri();
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(
            JSON_UTF_8,
            APPLICATION_JSON_LD
        );
    }

    @Override
    protected void validateRequest(I input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        attempt(() -> validate(requestInfo)).orElseThrow(
            failure -> new BadRequestException(failure.getException().getMessage()));
    }

    @Override
    protected Integer getSuccessStatusCode(I input, O output) {
        return HttpURLConnection.HTTP_OK;
    }

    protected abstract String getPathElement();

    private static boolean isServerError(ApiGatewayException e) {
        return e.getStatusCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR;
    }

    private static Function<Failure<ThirdPartyPublicationChannel>, ApiGatewayException> throwOriginalException(
        ApiGatewayException e) {
        return failure -> e;
    }
}
