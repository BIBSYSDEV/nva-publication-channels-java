package no.sikt.nva.pubchannels.handler.fetch.series;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import static nva.commons.core.attempt.Try.attempt;

public class FetchSeriesByIdentifierAndYearHandler extends FetchByIdentifierAndYearHandler<Void, Void> {

    @JacocoGenerated
    public FetchSeriesByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchSeriesByIdentifierAndYearHandler(Environment environment,
                                                 PublicationChannelClient publicationChannelClient) {
        super(Void.class, environment, publicationChannelClient);
    }

    @Override
    protected Void processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        attempt(() -> validate(requestInfo))
                .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));
        return null;
    }
}
