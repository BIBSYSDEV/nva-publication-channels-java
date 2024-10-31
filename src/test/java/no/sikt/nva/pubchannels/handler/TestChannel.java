package no.sikt.nva.pubchannels.handler;

import static no.sikt.nva.pubchannels.handler.TestUtils.scientificValueToLevel;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
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

public record TestChannel(String identifier,
                          Integer year,
                          String name,
                          ScientificValue scientificValue,
                          IsbnPrefix isbnPrefix,
                          Issn onlineIssn,
                          Issn printIssn,
                          String discontinued,
                          URI sameAs) {

    public TestChannel(Integer year, String identifier) {
        this(identifier, year, randomString(), randomElement(ScientificValue.values()), new IsbnPrefix(randomString()),
             new Issn(randomIssn()), new Issn(randomIssn()), randomString(), randomUri());
    }

    public TestChannel withName(String name) {
        return new TestChannel(identifier, year, name, scientificValue, isbnPrefix, onlineIssn, printIssn, discontinued,
                               sameAs);
    }

    public TestChannel withPrintIssn(String printIssn) {
        return new TestChannel(identifier, year, name, scientificValue, isbnPrefix, onlineIssn, new Issn(printIssn),
                               discontinued, sameAs);
    }

    public String asChannelRegistryJournalBody() {
        var channelRegistryBody = new ChannelRegistryJournal(identifier, name, onlineIssn.value(), printIssn.value(),
                                                             new ChannelRegistryLevel(year, scientificValueToLevel(
                                                                 scientificValue)),
                                                             sameAs,
                                                             discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public String asChannelRegistrySeriesBody() {
        var channelRegistryBody = new ChannelRegistrySeries(identifier, name, onlineIssn.value(), printIssn.value(),
                                                            new ChannelRegistryLevel(year, scientificValueToLevel(
                                                                scientificValue)),
                                                            sameAs,
                                                            discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public String asChannelRegistryPublisherBody() {
        var channelRegistryBody = new ChannelRegistryPublisher(identifier,
                                                               new ChannelRegistryLevel(year, scientificValueToLevel(
                                                                   scientificValue)), isbnPrefix.value(), name, sameAs,
                                                               discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public JournalDto asJournalDto(URI selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(selfUriBase, requestedYear);
        return new JournalDto(expectedId, identifier, name, onlineIssn.value(), printIssn.value(),
                              scientificValue, sameAs, discontinued, requestedYear);
    }

    public SeriesDto asSeriesDto(String selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(URI.create(selfUriBase), requestedYear);
        return new SeriesDto(expectedId, identifier, name, onlineIssn.value(), printIssn.value(), scientificValue,
                             sameAs,
                             discontinued, requestedYear);
    }

    public PublisherDto asPublisherDto(String selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(URI.create(selfUriBase), requestedYear);
        return new PublisherDto(expectedId, identifier, name, isbnPrefix.value(), scientificValue, sameAs, discontinued,
                                requestedYear);
    }

    private URI generateExpectedId(URI selfUriBase, String requestedYear) {
        return UriWrapper.fromUri(selfUriBase).addChild(identifier, requestedYear).getUri();
    }

    private record Issn(String value) {

    }

    private record IsbnPrefix(String value) {

    }
}
