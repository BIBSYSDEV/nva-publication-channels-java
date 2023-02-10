package no.sikt.nva.pubchannels.handler.request.validator;

import no.sikt.nva.pubchannels.handler.request.FetchJournalRequest;

public interface Validator {
    void validate(FetchJournalRequest request);
}
