package no.sikt.nva.pubchannels.handler.no.sikt.nva.pubchannels.handler.request.validator;

import static java.util.Objects.isNull;
import static nva.commons.core.attempt.Try.attempt;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.sikt.nva.pubchannels.handler.FetchJournalRequest;
import nva.commons.core.StringUtils;

public class FetchRequestValidator implements Validator {

    public static final String INVALID_YEAR_MESSAGE = "Invalid path parameter (year). Must be an integer between 1900"
                                                      + " and 2999.";
    private static final int MIN_ACCEPTABLE_YEAR = 2004;
    private static final String INVALID_IDENTIFIER_MESSAGE = "Invalid path parameter (identifier). Must be a UUID "
                                                             + "version 4.";
    public static final String DELIMITER = ", ";

    @Override
    public void validate(FetchJournalRequest request) {
        var errorMessage = Stream.of(validateYear(request.getYear()), validateIdentifier(request.getIdentifier()))
                               .filter(Optional::isPresent)
                               .map(Optional::get)
                               .collect(Collectors.joining(DELIMITER));
        if (!errorMessage.isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    private Optional<String> validateIdentifier(String identifier) {
        if (StringUtils.isEmpty(identifier) || isNotUuidTypeFour(identifier)) {
            return Optional.of(INVALID_IDENTIFIER_MESSAGE);
        }
        return Optional.empty();
    }

    private boolean isNotUuidTypeFour(String identifier) {
        var uuid = attempt(() -> UUID.fromString(identifier)).orElse(failure -> null);
        return isNull(uuid);
    }

    private Optional<String> validateYear(String year) {
        if (StringUtils.isEmpty(year) || isNotIntegerInGivenRange(year)) {
            return Optional.of(INVALID_YEAR_MESSAGE);
        }
        return Optional.empty();
    }

    private boolean isNotIntegerInGivenRange(String year) {
        var yearAsInteger = attempt(() -> Integer.parseInt(year)).orElse(fail -> -1);
        return yearAsInteger < MIN_ACCEPTABLE_YEAR || yearAsInteger > LocalDate.now().getYear() + 1;
    }
}
