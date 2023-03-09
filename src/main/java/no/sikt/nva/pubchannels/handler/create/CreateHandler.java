package no.sikt.nva.pubchannels.handler.create;

import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;

import static nva.commons.core.paths.UriWrapper.HTTPS;

public abstract class CreateHandler<I, O> extends ApiGatewayHandler<I, O> {
    private static final String ENV_DATAPORTEN_AUTH_BASE_URI = "ENV_DATAPORTEN_AUTH_BASE_URI";
    private static final String ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI =
            "ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI";
    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String ENV_DATAPORTEN_AUTH_CLIENT_ID = "ENV_DATAPORTEN_AUTH_CLIENT_ID";
    private static final String ENV_DATAPORTEN_AUTH_CLIENT_SECRET = "ENV_DATAPORTEN_AUTH_CLIENT_SECRET";
    protected PublicationChannelClient publicationChannelClient;

    @JacocoGenerated
    protected CreateHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
        var clientId = environment.readEnv(ENV_DATAPORTEN_AUTH_CLIENT_ID);
        var clientSecret = environment.readEnv(ENV_DATAPORTEN_AUTH_CLIENT_SECRET);
        var authBaseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_AUTH_BASE_URI));
        var pubChannelBaseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI));
        var authClient = new DataportenAuthClient(HttpClient.newBuilder().build(), authBaseUri, clientId, clientSecret);
        this.publicationChannelClient =
                new DataportenPublicationChannelClient(HttpClient.newBuilder().build(), pubChannelBaseUri, authClient);
    }

    protected CreateHandler(Class<I> requestClass, Environment environment, PublicationChannelClient client) {
        super(requestClass, environment);
        this.publicationChannelClient = client;
    }

    @Override
    protected Integer getSuccessStatusCode(I input, O output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    protected URI constructIdUri(String path, String pid) {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, path, pid)
                .getUri();
    }

    protected void userIsAuthorizedToCreate(RequestInfo requestInfo) throws UnauthorizedException {
        if (!requestInfo.userIsAuthorized(AccessRight.USER.toString())) {
            throw new UnauthorizedException();
        }
    }

}
