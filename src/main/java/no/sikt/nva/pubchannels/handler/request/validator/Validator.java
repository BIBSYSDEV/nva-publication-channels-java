package no.sikt.nva.pubchannels.handler.request.validator;

import no.sikt.nva.pubchannels.handler.request.FetchJournalRequest;
import org.apache.commons.validator.routines.ISSNValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.Objects;

public interface Validator {

    static void string(String value, int minLength, int maxLength) {
        Objects.requireNonNull(value, "String is required");
        if (value.length() < minLength) {
            throw new ValidationException("String is too short");
        }
        if (value.length() > maxLength) {
            throw new ValidationException("String is too long");
        }
    }

    static void optIssn(String issn) {
        if (issn != null && !ISSNValidator.getInstance().isValid(issn.trim())) {
            throw new ValidationException("Issn has an invalid format");
        }
    }

    static void optUrl(String url) {
        if (url != null && !UrlValidator.getInstance().isValid(url)) {
            throw new ValidationException("Url has an invalid format");
        }
    }

    void validate(FetchJournalRequest request);
}
