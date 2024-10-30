package no.sikt.nva.pubchannels.handler;

import static no.sikt.nva.pubchannels.handler.TestUtils.scientificValueToLevel;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryJournal;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySeries;
import no.sikt.nva.pubchannels.handler.model.JournalDto;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.sikt.nva.pubchannels.handler.model.SeriesDto;
import nva.commons.core.paths.UriWrapper;

public class TestChannel {
    private final String identifier;
    private final Integer year;
    private final String isbnPrefix;
    private final ScientificValue scientificValue;
    private final String discontinued;
    private final URI sameAs;
    private String name;
    private String onlineIssn;
    private String printIssn;

    private TestChannel(Integer year, String identifier, String name,
                        String onlineIssn, String printIssn, String isbnPrefix,
                        ScientificValue scientificValue,
                        String discontinued, URI sameAs) {

        this.identifier = identifier;
        this.year = year;
        this.name = name;
        this.onlineIssn = onlineIssn;
        this.printIssn = printIssn;
        this.isbnPrefix = isbnPrefix;
        this.scientificValue = scientificValue;
        this.discontinued = discontinued;
        this.sameAs = sameAs;
    }

    public TestChannel(Integer year, String identifier) {
        this(year, identifier, randomString(), randomString(), randomString(), randomString(),
             randomElement(ScientificValue.values()), randomString(), randomUri());
    }

    public TestChannel withName(String name) {
        this.name = name;
        return this;
    }

    public TestChannel withOnlineIssn(String onlineIssn) {
        this.onlineIssn = onlineIssn;
        return this;
    }

    public TestChannel withPrintIssn(String printIssn) {
        this.printIssn = printIssn;
        return this;
    }

    public JournalDto asJournalDto(URI selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(selfUriBase, requestedYear);
        return new JournalDto(expectedId, identifier, name, onlineIssn, printIssn,
                              scientificValue, sameAs, discontinued, requestedYear);
    }

    public String asChannelRegistryJournalBody() {
        var channelRegistryBody = new ChannelRegistryJournal(identifier, name, onlineIssn, printIssn,
                                                             new ChannelRegistryLevel(year, scientificValueToLevel(
                                                                 scientificValue)),
                                                             sameAs,
                                                             discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public String asChannelRegistryPublisherBody() {
        var channelRegistryBody = new ChannelRegistryPublisher(identifier,
                                                               new ChannelRegistryLevel(year, scientificValueToLevel(
                                                                   scientificValue)), isbnPrefix, name, sameAs,
                                                               discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public PublisherDto asPublisherDto(String selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(URI.create(selfUriBase), requestedYear);
        return new PublisherDto(expectedId, identifier, name, isbnPrefix, scientificValue, sameAs, discontinued,
                                requestedYear);
    }

    public String asChannelRegistrySeriesBody() {
        var channelRegistryBody = new ChannelRegistrySeries(identifier, name, onlineIssn, printIssn,
                                                            new ChannelRegistryLevel(year, scientificValueToLevel(
                                                                scientificValue)),
                                                            sameAs,
                                                            discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public SeriesDto asSeriesDto(String selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(URI.create(selfUriBase), requestedYear);
        return new SeriesDto(expectedId, identifier, name, onlineIssn, printIssn, scientificValue, sameAs,
                             discontinued, requestedYear);
    }

    private URI generateExpectedId(URI selfUriBase, String requestedYear) {
        return UriWrapper.fromUri(selfUriBase).addChild(identifier, requestedYear).getUri();
    }
}
