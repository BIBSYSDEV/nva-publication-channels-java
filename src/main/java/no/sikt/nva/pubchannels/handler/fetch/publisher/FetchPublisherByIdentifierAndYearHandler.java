package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static no.sikt.nva.pubchannels.channelRegistry.ChannelType.PUBLISHER;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelRegistry.PublicationChannelMovedException;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
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
        var request = new FetchByIdAndYearRequest(requestInfo);
        var publisherIdBaseUri = constructPublicationChannelIdBaseUri(PUBLISHER_PATH_ELEMENT);

        var requestYear = request.getYear();
        var publisher = fetchPublisher(request, requestYear);
        return FetchByIdAndYearResponse.create(publisherIdBaseUri,
                                               (ThirdPartyPublisher) publisher, requestYear);
    }

    private ThirdPartyPublicationChannel fetchPublisher(FetchByIdAndYearRequest request, String requestYear)
        throws ApiGatewayException {
        try {
            return publicationChannelClient.getChannel(PUBLISHER, request.getIdentifier(), requestYear);
        } catch (PublicationChannelMovedException movedException) {
            throw new PublicationChannelMovedException(
                "Publisher moved",
                constructNewLocation(PUBLISHER_PATH_ELEMENT, movedException.getLocation(), requestYear));
        }
    }
}
