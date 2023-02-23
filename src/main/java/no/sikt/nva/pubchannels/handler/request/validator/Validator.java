package no.sikt.nva.pubchannels.handler.request.validator;

import no.sikt.nva.pubchannels.handler.request.FetchJournalRequest;

public interface Validator {
    static void string(String value, int minLength, int maxLength) {
        if (value.length() < minLength) {
            throw new ValidationException("String is too short");
        }
        if (value.length() > maxLength) {
            throw new ValidationException("String is too long");
        }
    }

    void validate(FetchJournalRequest request);
}
