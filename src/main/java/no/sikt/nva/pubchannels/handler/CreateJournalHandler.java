package no.sikt.nva.pubchannels.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenAuthClient;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.model.CreateJournalRequest;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Map;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;

public class CreateJournalHandler extends ApiGatewayHandler<CreateJournalRequest, Void> {
    private static final String ENV_DATAPORTEN_AUTH_BASE_URI = "ENV_DATAPORTEN_AUTH_BASE_URI";
    private static final String ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI =
            "ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI";
    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    private static final String ENV_DATAPORTEN_AUTH_CLIENT_ID = "ENV_DATAPORTEN_AUTH_CLIENT_ID";
    private static final String ENV_DATAPORTEN_AUTH_CLIENT_SECRET = "ENV_DATAPORTEN_AUTH_CLIENT_SECRET";
    private static final String JOURNAL_PATH_ELEMENT = "journal";
    private final PublicationChannelClient publicationChannelClient;

    @JacocoGenerated
    public CreateJournalHandler() {
        super(CreateJournalRequest.class, new Environment());
        var clientId = environment.readEnv(ENV_DATAPORTEN_AUTH_CLIENT_ID);
        var clientSecret = environment.readEnv(ENV_DATAPORTEN_AUTH_CLIENT_SECRET);
        var authBaseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_AUTH_BASE_URI));
        var pubChannelBaseUri = URI.create(environment.readEnv(ENV_DATAPORTEN_PUBLICATION_CHANNEL_BASE_URI));
        var authClient = new DataportenAuthClient(HttpClient.newBuilder().build(), authBaseUri, clientId, clientSecret);
        publicationChannelClient =
                new DataportenPublicationChannelClient(HttpClient.newBuilder().build(), pubChannelBaseUri, authClient);
    }

    public CreateJournalHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(CreateJournalRequest.class, environment);
        this.publicationChannelClient = publicationChannelClient;
    }

    @Override
    protected Void processInput(CreateJournalRequest input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        var validInput = attempt(() -> validate(input))
                .orElseThrow(failure -> new BadRequestException(failure.getException().getMessage()));

        var journalPid = publicationChannelClient.createJournal(validInput.getName());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, constructJournalIdUri(journalPid).toString()));
        return null;
    }

    private CreateJournalRequest validate(CreateJournalRequest input) {
        validateString(input.getName(), 5, 300, "Name");
        validateOptionalIssn(input.getPrintIssn(), "PrintIssn");
        validateOptionalIssn(input.getOnlineIssn(), "OnlineIssn");
        validateOptionalUrl(input.getUrl(), "Url");
        return input;
    }

    @Override
    protected Integer getSuccessStatusCode(CreateJournalRequest input, Void output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    private URI constructJournalIdUri(String pid) {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, JOURNAL_PATH_ELEMENT, pid)
                .getUri();
    }
}
