package no.sikt.nva.pubchannels.handler.no.sikt.nva.pubchannels.handler.request.validator;

import no.sikt.nva.pubchannels.handler.FetchJournalRequest;

public interface Validator {
    void validate(FetchJournalRequest request) throws ValidationException;
}
