package no.sikt.nva.pubchannels.handler.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;

import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;


public class FetchJournalByIdentifierHandler extends ApiGatewayHandler<Void, FetchJournalByIdentifierResponse> {

    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    public static final String JOURNAL_PATH_ELEMENT = "journal";
    public static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";
    private final PublicationChannelClient publicationChannelClient;

    @JacocoGenerated
    public FetchJournalByIdentifierHandler() {
        super(Void.class, new Environment());
        this.publicationChannelClient = DataportenPublicationChannelClient.defaultInstance();
    }

    public FetchJournalByIdentifierHandler(Environment environment, PublicationChannelClient publicationChannelClient) {
        super(Void.class, environment);
        this.publicationChannelClient = publicationChannelClient;
    }

    @Override
    protected FetchJournalByIdentifierResponse processInput(Void input,
                                                            RequestInfo requestInfo,
                                                            Context context) throws ApiGatewayException {
        var pid = attempt(
                () -> requestInfo.getPathParameter(IDENTIFIER_PATH_PARAM_NAME).trim()).orElseThrow();

        var journalIdBaseUri = constructJournalIdBaseUri();

        return FetchJournalByIdentifierResponse.create(journalIdBaseUri,
                publicationChannelClient.getJournalByIdentifier(pid));
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, FetchJournalByIdentifierResponse output) {
        return HttpURLConnection.HTTP_OK;
    }

    private URI constructJournalIdBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, JOURNAL_PATH_ELEMENT)
                .getUri();
    }
}
