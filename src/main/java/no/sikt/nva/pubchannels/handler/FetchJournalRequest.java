package no.sikt.nva.pubchannels.handler;

import static nva.commons.core.attempt.Try.attempt;
import java.time.LocalDate;
import java.util.UUID;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.StringUtils;

public final class FetchJournalRequest {
    private static final String IDENTIFIER_PATH_PARAM_NAME = "identifier";
    private static final String YEAR_PATH_PARAM_NAME = "year";
    public static final String INVALID_YEAR_MESSAGE = "Invalid path parameter (year). Must be an integer between 1900"
                                                      + " and 2999.";
    private static final int MIN_ACCEPTABLE_YEAR = 2004;
    private static final String INVALID_IDENTIFIER_MESSAGE = "Invalid path parameter (identifier). Must be a UUID "
                                                             + "version 4.";
    private static final int UUID_TYPE_4_LENGTH = 36;
    private static final int YEAR_LENGTH = 4;


    private final String identifier;
    private final String year;
    private transient boolean validated;

    public FetchJournalRequest(RequestInfo requestInfo) {
        this.identifier = requestInfo.getPathParameter(IDENTIFIER_PATH_PARAM_NAME).trim();
        this.year = requestInfo.getPathParameter(YEAR_PATH_PARAM_NAME).trim();
    }

    public String getIdentifier() {
        if (!validated) {
            throw new IllegalStateException("Request data has not been validate. Call validate() first!");
        }
        return identifier;
    }

    public String getYear() {
        if (!validated) {
            throw new IllegalStateException("Request data has not been validate. Call validate() first!");
        }
        return year;
    }

    public void validate() throws BadRequestException {
        validateIdentifier();
        validateYear();
        this.validated = true;
    }

    private void validateYear() throws BadRequestException {
        if (StringUtils.isEmpty(year)) {
            throw new BadRequestException(INVALID_YEAR_MESSAGE);
        }

        if (year.length() != YEAR_LENGTH) {
            throw new BadRequestException(INVALID_YEAR_MESSAGE);
        }

        var yearAsInteger = attempt(() -> Integer.parseInt(year))
                                .orElseThrow(failure -> new BadRequestException(INVALID_YEAR_MESSAGE));

        var maxYear = LocalDate.now().getYear() + 1;
        if (yearAsInteger < MIN_ACCEPTABLE_YEAR || yearAsInteger > maxYear) {
            throw new BadRequestException(INVALID_YEAR_MESSAGE);
        }
    }

    private void validateIdentifier() throws BadRequestException {
        if (StringUtils.isEmpty(identifier)) {
            throw new BadRequestException(INVALID_IDENTIFIER_MESSAGE);
        }

        if (identifier.length() != UUID_TYPE_4_LENGTH) {
            throw new BadRequestException(INVALID_IDENTIFIER_MESSAGE);
        }

        attempt(() -> UUID.fromString(identifier))
            .orElseThrow(failure -> new BadRequestException(INVALID_IDENTIFIER_MESSAGE));
    }

}
