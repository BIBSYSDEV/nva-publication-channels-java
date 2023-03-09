package no.sikt.nva.pubchannels.handler.fetch;

import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Year;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateUuid;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateYear;
import static nva.commons.core.paths.UriWrapper.HTTPS;

public abstract class FetchByIdentifierAndYearHandler<I, O> extends ApiGatewayHandler<I, O> {
    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String YEAR_PATH_PARAM_NAME = "year";
    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";
    protected PublicationChannelClient publicationChannelClient;

    @JacocoGenerated
    protected FetchByIdentifierAndYearHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
        this.publicationChannelClient = DataportenPublicationChannelClient.defaultInstance();
    }

    protected FetchByIdentifierAndYearHandler(Class<I> requestClass, Environment environment,
                                              PublicationChannelClient client) {
        super(requestClass, environment);
        this.publicationChannelClient = client;
    }

    @Override
    protected Integer getSuccessStatusCode(I input, O output) {
        return HttpURLConnection.HTTP_OK;
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
}
