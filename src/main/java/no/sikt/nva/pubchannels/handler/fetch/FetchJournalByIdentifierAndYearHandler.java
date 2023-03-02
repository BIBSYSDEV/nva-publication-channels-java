package no.sikt.nva.pubchannels.handler.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Year;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateUuid;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateYear;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.HTTPS;

public class FetchJournalByIdentifierAndYearHandler extends ApiGatewayHandler<Void, FetchByIdAndYearResponse> {

    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    public static final String JOURNAL_PATH_ELEMENT = "journal";
    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";
    private static final String YEAR_PATH_PARAM_NAME = "year";
    public static final Year MIN_ACCEPTABLE_YEAR = Year.parse("2004");

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
    protected FetchByIdAndYearResponse processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        var request = attempt(() -> validate(requestInfo))
                .map(FetchByIdAndYearRequest::new)
                .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));

        var journalIdBaseUri = constructJournalIdBaseUri();

        return FetchByIdAndYearResponse.create(journalIdBaseUri,
                publicationChannelClient.getJournal(request.getIdentifier(), request.getYear()));
    }

    private RequestInfo validate(RequestInfo requestInfo) {
        validateUuid(requestInfo.getPathParameter(IDENTIFIER_PATH_PARAM_NAME).trim(), "Pid");
        validateYear(requestInfo.getPathParameter(YEAR_PATH_PARAM_NAME).trim(), MIN_ACCEPTABLE_YEAR,
                "Year");
        return requestInfo;
    }

    private URI constructJournalIdBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, JOURNAL_PATH_ELEMENT)
                .getUri();
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, FetchByIdAndYearResponse output) {
        return HttpURLConnection.HTTP_OK;
    }

}
