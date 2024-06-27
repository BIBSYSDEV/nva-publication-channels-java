package no.sikt.nva.pubchannels.handler.create.series;

import static no.sikt.nva.pubchannels.dataporten.ChannelType.SERIES;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreateSeriesRequest;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.validator.ValidationException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreateSeriesHandler extends CreateHandler<CreateSeriesRequest, CreateSeriesResponse> {

    private static final String SERIES_PATH_ELEMENT = "series";

    @JacocoGenerated
    public CreateSeriesHandler() {
        super(CreateSeriesRequest.class, new Environment());
    }

    public CreateSeriesHandler(Environment environment, DataportenPublicationChannelClient client) {
        super(CreateSeriesRequest.class, environment, client);
    }

    @Override
    protected void validateRequest(CreateSeriesRequest createSeriesRequest, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        validate(createSeriesRequest);
    }

    @Override
    protected CreateSeriesResponse processInput(CreateSeriesRequest input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        var response = publicationChannelClient.createSeries(getClientRequest(input));
        var createdUri = constructIdUri(SERIES_PATH_ELEMENT, response.getPid());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, createdUri.toString()));
        return CreateSeriesResponse.create(
            createdUri,
            (ThirdPartySeries) publicationChannelClient.getChannel(SERIES, response.getPid(), getYear()));
    }

    private static DataportenCreateSeriesRequest getClientRequest(CreateSeriesRequest request) {
        return new DataportenCreateSeriesRequest(
            request.name(),
            request.printIssn(),
            request.onlineIssn(),
            request.homepage());
    }

    private void validate(CreateSeriesRequest input) throws BadRequestException {
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
