package no.sikt.nva.pubchannels.handler;

import no.sikt.nva.pubchannels.dataporten.FetchJournalByIdentifierDto;
import no.sikt.nva.pubchannels.dataporten.model.CreateJournalResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface PublicationChannelClient {

    ThirdPartyJournal getJournalByIdentifierAndYear(String identifier, String year) throws ApiGatewayException;

    FetchJournalByIdentifierDto getJournalByIdentifier(String identifier) throws ApiGatewayException;

    CreateJournalResponse createJournal(String name) throws ApiGatewayException;
}
