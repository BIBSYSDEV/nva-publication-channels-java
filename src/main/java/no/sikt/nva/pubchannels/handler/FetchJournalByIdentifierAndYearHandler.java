package no.sikt.nva.pubchannels.handler;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.UUID;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelSource;
import no.sikt.nva.pubchannels.model.Journal;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

public class FetchJournalByIdentifierAndYearHandler extends ApiGatewayHandler<Void, Journal> {

    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";
    private static final String YEAR_PATH_PARAM_NAME = "year";
    public static final String INVALID_YEAR_MESSAGE = "Invalid path parameter (year). Must be an integer between 1900"
                                                      + " and 2999.";
    private static final int MIN_ACCEPTABLE_YEAR = 1900;
    private static final int MAX_ACCEPTABLE_YEAR = 2999;
    private static final String INVALID_IDENTIFIER_MESSAGE = "Invalid path parameter (identifier). Must be a UUID "
                                                             + "version 4.";
    private static final int UUID_TYPE_4_LENGTH = 36;
    private static final int YEAR_LENGTH = 4;

    private final transient PublicationChannelSource publicationChannelSource;

    @JacocoGenerated
    public FetchJournalByIdentifierAndYearHandler() {
        super(Void.class);
        this.publicationChannelSource = DataportenPublicationChannelSource.defaultInstance();
    }

    public FetchJournalByIdentifierAndYearHandler(PublicationChannelSource publicationChannelSource) {
        super(Void.class);
        this.publicationChannelSource = publicationChannelSource;
    }

    @Override
    protected Journal processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        var identifier = requestInfo.getPathParameter(IDENTIFIER_PATH_PARAM_NAME).trim();
        var year = requestInfo.getPathParameter(YEAR_PATH_PARAM_NAME).trim();

        validateInput(identifier, year);

        return publicationChannelSource.getJournal(identifier, year);
    }

    private void validateInput(String identifier, String year) throws BadRequestException {
        validateIdentifier(identifier);
        validateYear(year);
    }

    private void validateYear(String year) throws BadRequestException {
        if (StringUtils.isEmpty(year)) {
            throw new BadRequestException(INVALID_YEAR_MESSAGE);
        }

        if (year.length() != YEAR_LENGTH) {
            throw new BadRequestException(INVALID_YEAR_MESSAGE);
        }

        var yearAsInteger = attempt(() -> Integer.parseInt(year))
                                .orElseThrow(failure -> new BadRequestException(INVALID_YEAR_MESSAGE));

        if (yearAsInteger < MIN_ACCEPTABLE_YEAR || yearAsInteger > MAX_ACCEPTABLE_YEAR) {
            throw new BadRequestException(INVALID_YEAR_MESSAGE);
        }
    }

    private void validateIdentifier(String identifier) throws BadRequestException {
        if (StringUtils.isEmpty(identifier)) {
            throw new BadRequestException(INVALID_IDENTIFIER_MESSAGE);
        }

        if (identifier.length() != UUID_TYPE_4_LENGTH) {
            throw new BadRequestException(INVALID_IDENTIFIER_MESSAGE);
        }

        attempt(() -> UUID.fromString(identifier))
            .orElseThrow(failure -> new BadRequestException(INVALID_IDENTIFIER_MESSAGE));
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Journal output) {
        return HttpURLConnection.HTTP_OK;
    }

}
