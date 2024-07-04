package no.sikt.nva.pubchannels.handler.create;

import static java.util.Objects.isNull;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Year;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.channelRegistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import nva.commons.secrets.SecretsReader;

public abstract class CreateHandler<I, O> extends ApiGatewayHandler<I, O> {

    private static final String ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI = "DATAPORTEN_CHANNEL_REGISTRY_BASE_URL";
    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String SECRET_NAME = "DataportenChannelRegistryClientCredentials";
    private static final String CURRENT_YEAR = Year.now().toString();
    protected PublicationChannelClient publicationChannelClient;

    @JacocoGenerated
    protected CreateHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
        var secretsReader = new SecretsReader();
        var clientId = secretsReader.fetchSecret(SECRET_NAME, "clientId");
        var clientSecret = secretsReader.fetchSecret(SECRET_NAME, "clientSecret");
        var authBaseUri = URI.create(secretsReader.fetchSecret(SECRET_NAME, "authBaseUrl"));
        var pubChannelBaseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI));
        var authClient = new DataportenAuthClient(HttpClient.newBuilder().build(), authBaseUri, clientId, clientSecret);
        this.publicationChannelClient = new ChannelRegistryClient(HttpClient.newBuilder().build(),
                                                                  pubChannelBaseUri, authClient);
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
        return new UriWrapper(HTTPS, apiDomain).addChild(customDomainBasePath, path, pid, CURRENT_YEAR).getUri();
    }

    protected void userIsAuthorizedToCreate(RequestInfo requestInfo) throws UnauthorizedException {
        if (isNull(requestInfo.getCurrentCustomer())) {
            throw new UnauthorizedException();
        }
    }

    protected String getYear() {
        return String.valueOf(Year.now());
    }
}
