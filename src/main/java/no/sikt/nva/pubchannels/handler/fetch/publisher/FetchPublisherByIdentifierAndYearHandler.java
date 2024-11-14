package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.PUBLISHER;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.function.Function;
import no.sikt.nva.pubchannels.channelregistry.PublicationChannelMovedException;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelFetchClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchPublisherByIdentifierAndYearHandler extends
                                                      FetchByIdentifierAndYearHandler<Void, PublisherDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchPublisherByIdentifierAndYearHandler.class);
    private static final String PUBLISHER_PATH_ELEMENT = "publisher";

    @JacocoGenerated
    public FetchPublisherByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchPublisherByIdentifierAndYearHandler(Environment environment,
                                                    PublicationChannelClient publicationChannelClient,
                                                    PublicationChannelFetchClient cacheClient) {
        super(Void.class, environment, publicationChannelClient, cacheClient);
    }

    @Override
    protected PublisherDto processInput(Void input, RequestInfo requestInfo,
                                        Context context)
        throws ApiGatewayException {
        var request = new FetchByIdAndYearRequest(requestInfo);
        var publisherIdBaseUri = constructPublicationChannelIdBaseUri(PUBLISHER_PATH_ELEMENT);

        var requestYear = request.getYear();
        var publisher = shouldUseCache ? fetchPublisherFromCache(request) : fetchPublisherOrFetchFromCache(request);
        return PublisherDto.create(publisherIdBaseUri,
                                   (ThirdPartyPublisher) publisher, requestYear);
    }

    private ThirdPartyPublicationChannel fetchPublisherFromCache(FetchByIdAndYearRequest request)
        throws ApiGatewayException {
        LOGGER.info("Fetching publisher from cache: {}", request.getIdentifier());
        return cacheClient.getChannel(PUBLISHER, request.getIdentifier(), request.getYear());
    }

    private ThirdPartyPublicationChannel fetchPublisherOrFetchFromCache(FetchByIdAndYearRequest request)
        throws ApiGatewayException {
        try {
            return fetchPublisher(request, request.getYear());
        } catch (ApiGatewayException e) {
            return fetchFromCacheWhenServerError(request, e);
        }
    }

    private ThirdPartyPublicationChannel fetchFromCacheWhenServerError(FetchByIdAndYearRequest request,
        ApiGatewayException e)
        throws ApiGatewayException {
        if (e.getStatusCode() >= 500) {
            return attempt(() -> fetchPublisherFromCache(request)).orElseThrow(throwOriginalException(e));
        } else {
            throw e;
        }
    }

    private static Function<Failure<ThirdPartyPublicationChannel>, ApiGatewayException> throwOriginalException(
        ApiGatewayException e) {
        return failure -> e;
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
