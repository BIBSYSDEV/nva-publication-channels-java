package no.sikt.nva.pubchannels.handler.fetch.publisher;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.PUBLISHER;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.PublicationChannelClient;
import no.sikt.nva.pubchannels.handler.ThirdPartyPublisher;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdAndYearRequest;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.sikt.nva.pubchannels.utils.AppConfig;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchPublisherByIdentifierAndYearHandler extends FetchByIdentifierAndYearHandler<Void, PublisherDto> {

    private static final String PUBLISHER_PATH_ELEMENT = "publisher";

    @JacocoGenerated
    public FetchPublisherByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchPublisherByIdentifierAndYearHandler(Environment environment,
                                                    PublicationChannelClient publicationChannelClient,
                                                    CacheService cacheService, AppConfig appConfig) {
        super(Void.class, environment, publicationChannelClient, cacheService, appConfig);
    }

    @Override
    protected PublisherDto processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        var request = new FetchByIdAndYearRequest(requestInfo);
        var publisherIdBaseUri = constructPublicationChannelIdBaseUri(PUBLISHER_PATH_ELEMENT);
        var year = request.getYear();
        var identifier = request.getIdentifier();

        var publisher = super.shouldUseCache()
                            ? super.fetchChannelFromCache(PUBLISHER, identifier, year)
                            : super.fetchChannelOrFetchFromCache(PUBLISHER, identifier, year);
        return PublisherDto.create(publisherIdBaseUri, (ThirdPartyPublisher) publisher, year);
    }
}
