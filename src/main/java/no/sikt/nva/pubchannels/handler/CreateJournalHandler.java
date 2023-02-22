package no.sikt.nva.pubchannels.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.model.CreateJournalRequest;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;

import static nva.commons.core.paths.UriWrapper.HTTPS;

public class CreateJournalHandler extends ApiGatewayHandler<CreateJournalRequest, PidDto> {
    private static final String ENV_DATAPORTEN_AUTH_BASE_URI = "ENV_DATAPORTEN_AUTH_BASE_URI";
    private static final String ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI =
            "ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI";
    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String JOURNAL_PATH_ELEMENT = "JOURNAL_PATH_ELEMENT";
    private final PublicationChannelClient publicationChannelClient;
    private static final String ENV_DATAPORTEN_AUTH_CLIENT_ID = "ENV_DATAPORTEN_AUTH_CLIENT_ID";
    private static final String ENV_DATAPORTEN_AUTH_CLIENT_SECRET = "ENV_DATAPORTEN_AUTH_CLIENT_SECRET";

    @JacocoGenerated
    public CreateJournalHandler() {
        super(CreateJournalRequest.class, new Environment());
        String clientId = this.environment.readEnv(ENV_DATAPORTEN_AUTH_CLIENT_ID);
        String clientSecret = environment.readEnv(ENV_DATAPORTEN_AUTH_CLIENT_SECRET);
        URI authBaseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_AUTH_BASE_URI));
        URI pubChannelBaseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI));
        AuthClient authClient =
                new DataportenAuthClient(HttpClient.newBuilder().build(),authBaseUri, clientId, clientSecret);
        publicationChannelClient =
                new DataportenPublicationChannelClient(HttpClient.newBuilder().build(),pubChannelBaseUri, authClient);
    }

    public CreateJournalHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(CreateJournalRequest.class, environment);
        this.publicationChannelClient = publicationChannelClient;
    }

    @Override
    protected PidDto processInput(CreateJournalRequest input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        String journalPid = publicationChannelClient.createJournal(input.getName());
        URI uri = constructJournalIdBaseUri();
        return PidDto.create(uri, journalPid);
    }

    @Override
    protected Integer getSuccessStatusCode(CreateJournalRequest input, PidDto output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    private URI constructJournalIdBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, JOURNAL_PATH_ELEMENT)
                .getUri();
    }
}
