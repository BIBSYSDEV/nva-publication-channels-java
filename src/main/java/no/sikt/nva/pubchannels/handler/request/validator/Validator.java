package no.sikt.nva.pubchannels.handler.request.validator;

import no.sikt.nva.pubchannels.handler.request.FetchJournalRequest;

import java.util.regex.Pattern;

public interface Validator {

    Pattern VALID_ISSN_PATTERN = Pattern.compile("^[0-9]{4}-[0-9]{3}[0-9X]$");
    Pattern VALID_URL_PATTERN = Pattern.compile("^(https?|http?|ftp)://(.*)");

    static void string(String value, int minLength, int maxLength) {
        if (value.length() < minLength) {
            throw new ValidationException("String is too short");
        }
        if (value.length() > maxLength) {
            throw new ValidationException("String is too long");
        }
    }

    static void optIssn(String issn) {
        if (issn != null && !VALID_ISSN_PATTERN.matcher(issn).matches()) {
            throw new ValidationException("Issn has an invalid format");
        }
    }

    static void optUrl(String url) {
        if (url != null && !VALID_URL_PATTERN.matcher(url).matches()) {
            throw new ValidationException("Url has an invalid format");
        }
    }

    void validate(FetchJournalRequest request);
}
