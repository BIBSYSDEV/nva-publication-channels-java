package no.sikt.nva.pubchannels.handler.fetch.series;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.SERIES;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.function.Function;
import no.sikt.nva.pubchannels.channelregistry.PublicationChannelMovedException;
import no.sikt.nva.pubchannels.channelregistrycache.ChannelRegistryCsvCacheClient;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublicationChannel;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.SeriesDto;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

public class FetchSeriesByIdentifierAndYearHandler extends FetchByIdentifierAndYearHandler<Void, SeriesDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchSeriesByIdentifierAndYearHandler.class);
    private static final String SERIES_PATH_ELEMENT = "series";

    @JacocoGenerated
    public FetchSeriesByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchSeriesByIdentifierAndYearHandler(Environment environment,
                                                 PublicationChannelClient publicationChannelClient,
                                                 S3Client s3Client) {
        super(Void.class, environment, publicationChannelClient, s3Client);
    }

    @Override
    protected SeriesDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var request = new FetchByIdAndYearRequest(requestInfo);
        var publisherIdBaseUri = constructPublicationChannelIdBaseUri(SERIES_PATH_ELEMENT);

        var requestYear = request.getYear();
        var series = shouldUseCache ? fetchSeriesFromCache(request) : fetchSeriesOrFetchFromCache(request);
        return SeriesDto.create(publisherIdBaseUri, (ThirdPartySeries) series, requestYear);
    }

    private static Function<Failure<ThirdPartyPublicationChannel>, ApiGatewayException> throwOriginalException(
        ApiGatewayException e) {
        return failure -> e;
    }

    private ThirdPartyPublicationChannel fetchSeriesOrFetchFromCache(FetchByIdAndYearRequest request)
        throws ApiGatewayException {
        try {
            return fetchSeries(request, request.getYear());
        } catch (ApiGatewayException e) {
            return fetchFromCacheWhenServerError(request, e);
        }
    }

    private ThirdPartyPublicationChannel fetchFromCacheWhenServerError(FetchByIdAndYearRequest request,
                                                                       ApiGatewayException e)
        throws ApiGatewayException {
        if (e.getStatusCode() >= 500) {
            return attempt(() -> fetchSeriesFromCache(request)).orElseThrow(throwOriginalException(e));
        } else {
            throw e;
        }
    }

    private ThirdPartyPublicationChannel fetchSeriesFromCache(FetchByIdAndYearRequest request)
        throws ApiGatewayException {
        LOGGER.info("Fetching series from cache: {}", request.getIdentifier());
        return ChannelRegistryCsvCacheClient.load(s3Client).getChannel(SERIES, request.getIdentifier(), request.getYear());
    }

    private ThirdPartyPublicationChannel fetchSeries(FetchByIdAndYearRequest request, String requestYear)
        throws ApiGatewayException {
        try {
            return publicationChannelClient.getChannel(SERIES, request.getIdentifier(), requestYear);
        } catch (PublicationChannelMovedException movedException) {
            throw new PublicationChannelMovedException("Series moved", constructNewLocation(SERIES_PATH_ELEMENT,
                                                                                            movedException.getLocation(),
                                                                                            requestYear));
        }
    }
}
