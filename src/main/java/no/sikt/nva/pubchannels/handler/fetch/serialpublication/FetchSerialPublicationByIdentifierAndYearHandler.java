package no.sikt.nva.pubchannels.handler.fetch.serialpublication;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistrycache.db.service.CacheService;
import no.sikt.nva.pubchannels.handler.fetch.FetchByIdentifierAndYearHandler;
import no.sikt.nva.pubchannels.handler.search.serialpublication.SerialPublicationDto;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchSerialPublicationByIdentifierAndYearHandler
    extends FetchByIdentifierAndYearHandler<Void, SerialPublicationDto> {

    @JacocoGenerated
    public FetchSerialPublicationByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
    }

    public FetchSerialPublicationByIdentifierAndYearHandler(Environment environment,
                                                            ChannelRegistryClient channelRegistryClient,
                                                            CacheService cacheService) {
        super(Void.class, environment, channelRegistryClient, cacheService);
    }

    @Override
    protected SerialPublicationDto processInput(Void unused, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        return null;
    }
}
