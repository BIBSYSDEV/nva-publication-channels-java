package no.sikt.nva.pubchannels.handler.fetch.series;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchSeriesByIdentifierAndYearHandler extends
                                                   FetchByIdentifierAndYearHandler<Void, FetchByIdAndYearResponse> {

    private static final String SERIES_PATH_ELEMENT = "series";

    @JacocoGenerated
    public FetchSeriesByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchSeriesByIdentifierAndYearHandler(Environment environment,
                                                 PublicationChannelClient publicationChannelClient) {
        super(Void.class, environment, publicationChannelClient);
    }

    @Override
    protected FetchByIdAndYearResponse processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        var request = attempt(() -> validate(requestInfo))
                          .map(FetchByIdAndYearRequest::new)
                          .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));

        var publisherIdBaseUri = constructPublicationChannelIdBaseUri(SERIES_PATH_ELEMENT);

        var requestYear = request.getYear();
        return FetchByIdAndYearResponse.create(publisherIdBaseUri,
                                               (ThirdPartySeries) publicationChannelClient.getChannel(
                                                   ChannelType.SERIES,
                                                   request.getIdentifier(), requestYear), requestYear);
    }
}
