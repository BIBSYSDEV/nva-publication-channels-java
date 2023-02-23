package no.sikt.nva.pubchannels.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.request.FetchJournalRequest;
import no.sikt.nva.pubchannels.model.JournalDto;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;

import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;

public class FetchJournalByIdentifierAndYearHandler extends ApiGatewayHandler<Void, JournalDto> {

    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    public static final String JOURNAL_PATH_ELEMENT = "journal";

    private final PublicationChannelClient publicationChannelClient;

    @JacocoGenerated
    public FetchJournalByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
        this.publicationChannelClient = DataportenPublicationChannelClient.defaultInstance();
    }

    public FetchJournalByIdentifierAndYearHandler(Environment environment,
                                                  PublicationChannelClient publicationChannelClient) {
        super(Void.class, environment);
        this.publicationChannelClient = publicationChannelClient;
    }

    @Override
    protected JournalDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        var request = attempt(() -> new FetchJournalRequest(requestInfo))
                .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));

        var journalIdBaseUri = constructJournalIdBaseUri();

        return JournalDto.create(journalIdBaseUri, publicationChannelClient.getJournal(request.getIdentifier(),
                request.getYear()));
    }

    private URI constructJournalIdBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, JOURNAL_PATH_ELEMENT)
                .getUri();
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, JournalDto output) {
        return HttpURLConnection.HTTP_OK;
    }

}
