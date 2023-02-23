package no.sikt.nva.pubchannels.handler.validator;

import org.apache.commons.validator.routines.ISSNValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.time.Year;
import java.util.Objects;
import java.util.UUID;

import static java.lang.String.format;
import static nva.commons.core.attempt.Try.attempt;

public interface Validator {

    static void validateString(String value, int minLength, int maxLength, String name) {
        Objects.requireNonNull(value, format("%s is required.", name));
        if (value.length() < minLength) {
            throw new ValidationException(format("%s is too short. Minimum length is %d", name, minLength));
        }
        if (value.length() > maxLength) {
            throw new ValidationException(format("%s is too long. Maximum length is %d", name, maxLength));
        }
    }

    static void validateOptionalIssn(String issn, String name) {
        if (issn != null && !ISSNValidator.getInstance().isValid(issn.trim())) {
            throw new ValidationException(format("%s has an invalid ISSN format.", name));
        }
    }

    static void validateOptionalUrl(String url, String name) {
        if (url != null && !UrlValidator.getInstance().isValid(url)) {
            throw new ValidationException(format("%s has an invalid URL format", name));
        }
    }

    static void validateUuid(String value, String name) {
        Objects.requireNonNull(value, format("%s is required.", name));
        attempt(() -> UUID.fromString(value))
                .orElseThrow(failure ->
                        new ValidationException(format("%s has an invalid UUIDv4 format", name)));
    }

    static void validateYear(String value, Year minAcceptableYear, String name) {
        Objects.requireNonNull(value, format("%s is required.", name));
        var year = attempt(() -> Year.parse(value))
                .orElseThrow(failure -> new ValidationException(format("%s field is not a valid year.", name)));
        var now = Year.now();
        if (year.isBefore(minAcceptableYear) || year.isAfter(now.plusYears(1))) {
            throw new ValidationException(
                    format("%s is not between the year %d and %d", name, minAcceptableYear.getValue(), now.getValue()));
        }

    }
}
