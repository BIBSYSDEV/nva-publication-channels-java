package no.sikt.nva.pubchannels.handler.fetch.series;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.SERIES;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistry.PublicationChannelMovedException;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
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
        var request = new FetchByIdAndYearRequest(requestInfo);
        var publisherIdBaseUri = constructPublicationChannelIdBaseUri(SERIES_PATH_ELEMENT);

        var requestYear = request.getYear();
        var series = fetchSeries(request, requestYear);
        return FetchByIdAndYearResponse.create(publisherIdBaseUri, (ThirdPartySeries) series, requestYear);
    }

    private ThirdPartyPublicationChannel fetchSeries(FetchByIdAndYearRequest request, String requestYear)
        throws ApiGatewayException {
        try {
            return publicationChannelClient.getChannel(SERIES, request.getIdentifier(), requestYear);
        } catch (PublicationChannelMovedException movedException) {
            throw new PublicationChannelMovedException(
                "Series moved",
                constructNewLocation(SERIES_PATH_ELEMENT, movedException.getLocation(), requestYear));
        }
    }
}
