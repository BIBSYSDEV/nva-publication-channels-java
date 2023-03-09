package no.sikt.nva.pubchannels.handler.create.series;

import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.create.DataportenCreateSeriesRequest;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.util.Map;

import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static nva.commons.core.attempt.Try.attempt;

public class CreateSeriesHandler extends CreateHandler<CreateSeriesRequest, Void> {

    private static final String PATH_ELEMENT = "series";

    @JacocoGenerated
    public CreateSeriesHandler() {
        super(CreateSeriesRequest.class, new Environment());
    }

    public CreateSeriesHandler(Environment environment, DataportenPublicationChannelClient client) {
        super(CreateSeriesRequest.class, environment, client);
    }

    @Override
    protected Void processInput(CreateSeriesRequest input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        var validInput = attempt(() -> validate(input))
                .map(CreateSeriesHandler::getClientRequest)
                .orElseThrow(failure -> new BadRequestException(failure.getException().getMessage()));
        var pid = publicationChannelClient.createSeries(validInput);
        var createdUri = constructIdUri(PATH_ELEMENT, pid.getPid());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, createdUri.toString()));
        return null;
    }

    private static DataportenCreateSeriesRequest getClientRequest(CreateSeriesRequest request) {
        return new DataportenCreateSeriesRequest(
                request.getName(),
                request.getPrintIssn(),
                request.getOnlineIssn(),
                request.getHomepage());

    }

    private CreateSeriesRequest validate(CreateSeriesRequest input) {
        validateString(input.getName(), 5, 300, "Name");
        validateOptionalIssn(input.getPrintIssn(), "PrintIssn");
        validateOptionalIssn(input.getOnlineIssn(), "OnlineIssn");
        validateOptionalUrl(input.getHomepage(), "Homepage");
        return input;
    }

}
