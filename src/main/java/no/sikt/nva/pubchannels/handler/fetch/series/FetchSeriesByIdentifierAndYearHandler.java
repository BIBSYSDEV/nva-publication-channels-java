package no.sikt.nva.pubchannels.handler.fetch.series;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.SERIES;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdAndYearRequest;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.SeriesDto;
import no.sikt.nva.pubchannels.utils.ApplicationConfiguration;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchSeriesByIdentifierAndYearHandler extends FetchByIdentifierAndYearHandler<Void, SeriesDto> {

    private static final String SERIES_PATH_ELEMENT = "series";

    @JacocoGenerated
    public FetchSeriesByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchSeriesByIdentifierAndYearHandler(Environment environment,
                                                 PublicationChannelClient publicationChannelClient,
                                                 CacheService cacheService, ApplicationConfiguration applicationConfiguration) {
        super(Void.class, environment, publicationChannelClient, cacheService, applicationConfiguration);
    }

    @Override
    protected SeriesDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var request = new FetchByIdAndYearRequest(requestInfo);
        var publisherIdBaseUri = constructPublicationChannelIdBaseUri(SERIES_PATH_ELEMENT);
        var year = request.getYear();
        var identifier = request.getIdentifier();

        var series = super.shouldUseCache()
                         ? super.fetchChannelFromCache(SERIES, identifier, year)
                         : super.fetchChannelOrFetchFromCache(SERIES, identifier, year);
        return SeriesDto.create(publisherIdBaseUri, (ThirdPartySerialPublication) series, year);
    }
}
