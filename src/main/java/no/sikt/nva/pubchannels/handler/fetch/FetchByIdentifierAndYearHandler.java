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
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import software.amazon.awssdk.services.s3.S3Client;

public abstract class FetchByIdentifierAndYearHandler<I, O> extends ApiGatewayHandler<I, O> {

    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String YEAR_PATH_PARAM_NAME = "year";
    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";

    protected final PublicationChannelClient publicationChannelClient;
    protected final S3Client s3Client;
    protected final boolean shouldUseCache;

    @JacocoGenerated
    protected FetchByIdentifierAndYearHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
        this.publicationChannelClient = ChannelRegistryClient.defaultInstance();
        this.s3Client = S3Client.create();
        this.shouldUseCache = Boolean.parseBoolean(environment.readEnv("SHOULD_USE_CACHE"));
    }

    protected FetchByIdentifierAndYearHandler(Class<I> requestClass, Environment environment,
                                              PublicationChannelClient client, S3Client s3Client) {
        super(requestClass, environment);
        this.publicationChannelClient = client;
        this.s3Client = s3Client;
        this.shouldUseCache = Boolean.parseBoolean(environment.readEnv("SHOULD_USE_CACHE"));
    }

    protected RequestInfo validate(RequestInfo requestInfo) {
        validateUuid(requestInfo.getPathParameter(IDENTIFIER_PATH_PARAM_NAME).trim(), "Pid");
        validateYear(requestInfo.getPathParameter(YEAR_PATH_PARAM_NAME).trim(), Year.of(Year.MIN_VALUE),
                     "Year");
        return requestInfo;
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
}
