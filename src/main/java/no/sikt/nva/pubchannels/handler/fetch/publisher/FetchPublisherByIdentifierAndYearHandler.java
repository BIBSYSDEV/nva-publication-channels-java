package no.sikt.nva.pubchannels.handler.fetch.publisher;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdAndYearRequest;
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

public class FetchPublisherByIdentifierAndYearHandler extends ApiGatewayHandler<Void, FetchByIdAndYearResponse> {

    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    public static final String PUBLISHER_PATH_ELEMENT = "publisher";
    private static final String YEAR_PATH_PARAM_NAME = "year";
    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";

    private final DataportenPublicationChannelClient publicationChannelClient;

    @JacocoGenerated
    public FetchPublisherByIdentifierAndYearHandler(DataportenPublicationChannelClient publicationChannelClient) {
        super(Void.class, new Environment());
        this.publicationChannelClient = publicationChannelClient;
    }

    public FetchPublisherByIdentifierAndYearHandler(Environment environment,
                                                    DataportenPublicationChannelClient publicationChannelClient) {
        super(Void.class, environment);
        this.publicationChannelClient = publicationChannelClient;
    }

    @Override
    protected FetchByIdAndYearResponse processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        var request = attempt(() -> validate(requestInfo))
                .map(FetchByIdAndYearRequest::new)
                .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));

        var publisherIdBaseUri = constructJournalIdBaseUri();

        return FetchByIdAndYearResponse.create(publisherIdBaseUri,
                publicationChannelClient.getPublisher(request.getIdentifier(), request.getYear()));

    }

    @Override
    protected Integer getSuccessStatusCode(Void input, FetchByIdAndYearResponse output) {
        return HttpURLConnection.HTTP_OK;
    }

    private RequestInfo validate(RequestInfo requestInfo) {
        validateUuid(requestInfo.getPathParameter(IDENTIFIER_PATH_PARAM_NAME).trim(), "Pid");
        validateYear(requestInfo.getPathParameter(YEAR_PATH_PARAM_NAME).trim(), Year.of(Year.MIN_VALUE),
                "Year");
        return requestInfo;
    }

    private URI constructJournalIdBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                .addChild(customDomainBasePath, PUBLISHER_PATH_ELEMENT)
                .getUri();
    }
}
