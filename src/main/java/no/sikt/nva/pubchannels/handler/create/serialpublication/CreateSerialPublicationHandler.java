package no.sikt.nva.pubchannels.handler.create.serialpublication;

import static java.util.Objects.isNull;
import static no.sikt.nva.pubchannels.channelregistry.ChannelType.SERIAL_PUBLICATION;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalIssn;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateOptionalUrl;
import static no.sikt.nva.pubchannels.handler.validator.Validator.validateString;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Locale;
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

public class CreateSerialPublicationHandler
    extends CreateHandler<CreateSerialPublicationRequest, SerialPublicationDto> {

    private static final String JOURNAL = "journal";
    private static final String SERIES = "series";
    private static final String CUSTOM_PATH_ELEMENT = "serial-publication";

    public CreateSerialPublicationHandler(Environment environment, ChannelRegistryClient channelRegistryClient) {
        super(CreateSerialPublicationRequest.class, environment, channelRegistryClient);
    }

    @Override
    protected void validateRequest(CreateSerialPublicationRequest createSerialPublicationRequest,
                                   RequestInfo requestInfo, Context context) throws ApiGatewayException {
        userIsAuthorizedToCreate(requestInfo);
        validate(createSerialPublicationRequest);
    }

    @Override
    protected SerialPublicationDto processInput(CreateSerialPublicationRequest request,
                                                RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var response = JOURNAL.equals(request.type())
                           ? publicationChannelClient.createJournal(getClientRequest(request))
                           : publicationChannelClient.createSeries(getClientRequest(request));

        // Fetch the new channel to build the full response
        var year = getYear();
        var newSerialPublication = (ThirdPartySerialPublication) publicationChannelClient.getChannel(SERIAL_PUBLICATION,
                                                                                                     response.pid(),
                                                                                                     year);
        var responseBody = SerialPublicationDto.create(constructBaseUri(CUSTOM_PATH_ELEMENT), newSerialPublication,
                                                       year);

        addAdditionalHeaders(() -> Map.of(HttpHeaders.LOCATION, responseBody.id().toString()));
        return responseBody;
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
            validateType(input.type());
            validateString(input.name(), 5, 300, "Name");
            validateOptionalIssn(input.printIssn(), "PrintIssn");
            validateOptionalIssn(input.onlineIssn(), "OnlineIssn");
            validateOptionalUrl(input.homepage(), "Homepage");
        } catch (ValidationException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }

    private void validateType(String type) {
        if (isNull(type)) {
            throw new ValidationException("Type cannot be null! Type must be either 'Journal' or 'Series'");
        }
        var typeLowerCase = type.toLowerCase(Locale.ROOT);
        if (!JOURNAL.equals(typeLowerCase) && !SERIES.equals(typeLowerCase)) {
            throw new ValidationException("Type must be either 'Journal' or 'Series'");
        }
    }
}
