package no.sikt.nva.pubchannels.handler;

import static no.sikt.nva.pubchannels.handler.TestUtils.scientificValueToLevel;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.handler.model.JournalDto;
import nva.commons.core.paths.UriWrapper;

public class TestData {

    private final String identifier;
    private final Integer year;
    private final String name;
    private final String originalTitle;
    private final String onlineIssn;
    private final String printIssn;
    private final String isbnPrefix;
    private final ScientificValue scientificValue;
    private final String discontinued;
    private final String sameAs;
    private final Integer totalResults;

    private TestData(Integer year, String identifier, String name,
                     String originalTitle, String onlineIssn, String printIssn, String isbnPrefix,
                     ScientificValue scientificValue,
                     String discontinued, String sameAs, Integer totalResults) {

        this.identifier = identifier;
        this.year = year;
        this.name = name;
        this.originalTitle = originalTitle;
        this.onlineIssn = onlineIssn;
        this.printIssn = printIssn;
        this.isbnPrefix = isbnPrefix;
        this.scientificValue = scientificValue;
        this.discontinued = discontinued;
        this.sameAs = sameAs;
        this.totalResults = totalResults;
    }

    public TestData(Integer year, String identifier) {
        this(year, identifier, randomString(), randomString(), randomString(), randomString(), randomString(),
             randomElement(ScientificValue.values()), randomString(), randomString(), randomInteger());
    }

    public JournalDto asJournalDto(URI selfUriBase, String requestedYear) {
        var expectedId = UriWrapper.fromUri(selfUriBase).addChild(identifier, requestedYear).getUri();
        return new JournalDto(expectedId, identifier, name, onlineIssn, printIssn,
                              scientificValue, URI.create(sameAs), discontinued, requestedYear);
    }

    public String asChannelRegistryJournalBody() {
        var channelRegistryBody = new ChannelRegistryJournal(identifier, name, onlineIssn, printIssn,
                                                             new ChannelRegistryLevel(year, scientificValueToLevel(
                                                                 scientificValue)),
                                                             URI.create(sameAs),
                                                             discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }
}
