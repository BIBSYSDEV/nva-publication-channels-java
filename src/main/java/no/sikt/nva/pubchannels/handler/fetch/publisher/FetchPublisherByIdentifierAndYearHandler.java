package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.dataporten.ChannelType;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchPublisherByIdentifierAndYearHandler extends
                                                      FetchByIdentifierAndYearHandler<Void, FetchByIdAndYearResponse> {

    private static final String PUBLISHER_PATH_ELEMENT = "publisher";

    @JacocoGenerated
    public FetchPublisherByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchPublisherByIdentifierAndYearHandler(Environment environment,
                                                    PublicationChannelClient publicationChannelClient) {
        super(Void.class, environment, publicationChannelClient);
    }

    @Override
    protected FetchByIdAndYearResponse processInput(Void input, RequestInfo requestInfo,
                                                    Context context)
        throws ApiGatewayException {
        var request = attempt(() -> validate(requestInfo))
                          .map(FetchByIdAndYearRequest::new)
                          .orElseThrow(fail -> new BadRequestException(fail.getException().getMessage()));

        var publisherIdBaseUri = constructPublicationChannelIdBaseUri(PUBLISHER_PATH_ELEMENT);

        return FetchByIdAndYearResponse.create(publisherIdBaseUri,
                                               (ThirdPartyPublisher) publicationChannelClient.getChannel(
                                                   ChannelType.PUBLISHER,
                                                   request.getIdentifier(),
                                                   request.getYear()));
    }
}
