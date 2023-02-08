package no.sikt.nva.pubchannels.handler;

import static nva.commons.core.paths.UriWrapper.HTTPS;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.URI;
import no.sikt.nva.pubchannels.dataporten.DataportenPublicationChannelSource;
import no.sikt.nva.pubchannels.model.JournalDto;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

public class FetchJournalByIdentifierAndYearHandler extends ApiGatewayHandler<Void, JournalDto> {

    private static final String ENV_API_DOMAIN = "API_DOMAIN";
    private static final String ENV_CUSTOM_DOMAIN_BASE_PATH = "CUSTOM_DOMAIN_BASE_PATH";
    public static final String JOURNAL_PATH_ELEMENT = "journal";

    private final transient PublicationChannelSource publicationChannelSource;

    @JacocoGenerated
    public FetchJournalByIdentifierAndYearHandler() {
        super(Void.class, new Environment());
        this.publicationChannelSource = DataportenPublicationChannelSource.defaultInstance();
    }

    public FetchJournalByIdentifierAndYearHandler(Environment environment,
                                                  PublicationChannelSource publicationChannelSource) {
        super(Void.class, environment);
        this.publicationChannelSource = publicationChannelSource;
    }

    @Override
    protected JournalDto processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        var request = new FetchJournalRequest(requestInfo);

        URI journalIdBaseUri = constructJournalIdBaseUri();

        return JournalDto.create(journalIdBaseUri, publicationChannelSource.getJournal(request.getIdentifier(),
                                                                                       request.getYear()));
    }

    private URI constructJournalIdBaseUri() {
        var apiDomain = environment.readEnv(ENV_API_DOMAIN);
        var customDomainBasePath = environment.readEnv(ENV_CUSTOM_DOMAIN_BASE_PATH);
        return new UriWrapper(HTTPS, apiDomain)
                              .addChild(customDomainBasePath, JOURNAL_PATH_ELEMENT)
                              .getUri();
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, JournalDto output) {
        return HttpURLConnection.HTTP_OK;
    }

}
