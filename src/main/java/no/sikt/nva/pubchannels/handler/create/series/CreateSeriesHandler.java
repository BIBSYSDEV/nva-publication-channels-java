package no.sikt.nva.pubchannels.handler.create.series;

import static no.sikt.nva.pubchannels.channelregistry.ChannelType.SERIES;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryClient;
import no.sikt.nva.pubchannels.channelregistry.model.create.ChannelRegistryCreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.ThirdPartySerialPublication;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.create.CreateSerialPublicationRequest;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.sikt.nva.pubchannels.handler.validator.ValidationException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreateSeriesHandler extends CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> {

    private static final String SERIES_PATH_ELEMENT = "series";

    @JacocoGenerated
    public CreateSeriesHandler() {
        super(CreateSerialPublicationRequest.class, new Environment());
    }

    public CreateSeriesHandler(Environment environment, ChannelRegistryClient client) {
        super(CreateSerialPublicationRequest.class, environment, client);
    }

    @Override
    protected void validateRequest(CreateSerialPublicationRequest createSeriesRequest, RequestInfo requestInfo,
                                   Context context)
        throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        validate(createSeriesRequest);
    }

    @Override
    protected SerialPublicationDto processInput(CreateSerialPublicationRequest input, RequestInfo requestInfo,
                                                Context context)
        throws ApiGatewayException {
        var response = publicationChannelClient.createSeries(getClientRequest(input));

        // Fetch the new series from the channel registry to build the full response
        var year = getYear();
        var newSeries = (ThirdPartySerialPublication) publicationChannelClient.getChannel(SERIES,
                                                                                          response.pid(),
                                                                                          year);
        var seriesDto = SerialPublicationDto.create(constructBaseUri(SERIES_PATH_ELEMENT), newSeries, year);

        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, seriesDto.id().toString()));
        return seriesDto;
    }

    private static ChannelRegistryCreateSerialPublicationRequest getClientRequest(
        CreateSerialPublicationRequest request) {
        return new ChannelRegistryCreateSerialPublicationRequest(request.name(),
                                                                 request.printIssn(),
                                                                 request.onlineIssn(),
                                                                 request.homepage());
    }

    private void validate(CreateSerialPublicationRequest input) throws BadRequestException {
        try {
            validateString(input.name(), 5, 300, "Name");
            validateOptionalIssn(input.printIssn(), "PrintIssn");
            validateOptionalIssn(input.onlineIssn(), "OnlineIssn");
            validateOptionalUrl(input.homepage(), "Homepage");
        } catch (ValidationException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }
}
