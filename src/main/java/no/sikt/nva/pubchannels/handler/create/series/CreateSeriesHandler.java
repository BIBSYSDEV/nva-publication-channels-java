package no.sikt.nva.pubchannels.handler.create.series;

import static no.sikt.nva.pubchannels.dataporten.ChannelType.SERIES;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Calendar;
import java.util.Map;
import no.sikt.nva.pubchannels.HttpHeaders;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelClient;
import no.sikt.nva.pubchannels.dataporten.model.create.DataportenCreateSeriesRequest;
import no.sikt.nva.pubchannels.handler.ThirdPartySeries;
import no.sikt.nva.pubchannels.handler.create.CreateHandler;
import no.sikt.nva.pubchannels.handler.fetch.series.FetchByIdAndYearResponse;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreateSeriesHandler extends CreateHandler<CreateSeriesRequest, FetchByIdAndYearResponse> {

    private static final String SERIES_PATH_ELEMENT = "series";

    @JacocoGenerated
    public CreateSeriesHandler() {
        super(CreateSeriesRequest.class, new Environment());
    }

    public CreateSeriesHandler(Environment environment, DataportenPublicationChannelClient client) {
        super(CreateSeriesRequest.class, environment, client);
    }

    @Override
    protected FetchByIdAndYearResponse processInput(CreateSeriesRequest input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        var validInput = attempt(() -> validate(input))
                             .map(CreateSeriesHandler::getClientRequest)
                             .orElseThrow(failure -> new BadRequestException(failure.getException().getMessage()));
        var createResponse = publicationChannelClient.createSeries(validInput);
        var createdUri = constructIdUri(SERIES_PATH_ELEMENT, createResponse.getPid());
        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, createdUri.toString()));
        return FetchByIdAndYearResponse.create(
            createdUri,
            (ThirdPartySeries) publicationChannelClient.getChannel(SERIES, createResponse.getPid(), getYear()),
            getYear());
    }

    private static String getYear() {
        return String.valueOf(Calendar.getInstance().getWeekYear());
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
