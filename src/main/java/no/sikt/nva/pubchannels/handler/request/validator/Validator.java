package no.sikt.nva.pubchannels.handler.request.validator;

import no.sikt.nva.pubchannels.handler.request.FetchJournalRequest;
import org.apache.commons.validator.routines.ISSNValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.Objects;

public interface Validator {

    static void validateString(String value, int minLength, int maxLength, String name) {
        Objects.requireNonNull(value, String.format("%s is required.", name));
        if (value.length() < minLength) {
            throw new ValidationException(String.format("%s is too short. Minimum length is %d", name, minLength));
        }
        if (value.length() > maxLength) {
            throw new ValidationException(String.format("%s is too long. Maximum length is %d", name, maxLength));
        }
    }

    static void validateOptionalIssn(String issn, String name) {
        if (issn != null && !ISSNValidator.getInstance().isValid(issn.trim())) {
            throw new ValidationException(String.format("%s has an invalid ISSN format.", name));
        }
    }

    static void validateOptionalUrl(String url, String name) {
        if (url != null && !UrlValidator.getInstance().isValid(url)) {
            throw new ValidationException(String.format("%s has an invalid URL format", name));
        }
    }

    void validate(FetchJournalRequest request);
}
