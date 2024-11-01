package no.sikt.nva.pubchannels.handler;

import static no.sikt.nva.pubchannels.handler.TestUtils.scientificValueToLevel;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
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
        return new ChannelRegistryJournal(identifier,
                                          name,
                                          onlineIssn.value(),
                                          printIssn.value(),
                                          new ChannelRegistryLevel(year, scientificValueToLevel(scientificValue)),
                                          sameAs,
                                          discontinued)
                   .toJsonString();
    }

    public String asChannelRegistrySeriesBody() {
        return new ChannelRegistrySeries(identifier,
                                         name,
                                         onlineIssn.value(),
                                         printIssn.value(),
                                         new ChannelRegistryLevel(year, scientificValueToLevel(scientificValue)),
                                         sameAs,
                                         discontinued)
                   .toJsonString();
    }

    public String asChannelRegistryPublisherBody() {
        return new ChannelRegistryPublisher(identifier,
                                            new ChannelRegistryLevel(year, scientificValueToLevel(scientificValue)),
                                            isbnPrefix.value(),
                                            name,
                                            sameAs,
                                            discontinued)
                   .toJsonString();
    }

    public JournalDto asJournalDto(URI selfUriBase, String requestedYear) {
        var id = generateIdWithYear(selfUriBase, requestedYear);
        return new JournalDto(id, identifier, name, onlineIssn.value(), printIssn.value(),
                              scientificValue, sameAs, discontinued, requestedYear);
    }

    public SeriesDto asSeriesDto(String selfUriBase, String requestedYear) {
        var id = generateIdWithYear(URI.create(selfUriBase), requestedYear);
        return new SeriesDto(id, identifier, name, onlineIssn.value(), printIssn.value(), scientificValue,
                             sameAs,
                             discontinued, requestedYear);
    }

    public PublisherDto asPublisherDto(String selfUriBase, String requestedYear) {
        var id = generateIdWithYear(URI.create(selfUriBase), requestedYear);
        return new PublisherDto(id, identifier, name, isbnPrefix.value(), scientificValue, sameAs, discontinued,
                                requestedYear);
    }

    private URI generateIdWithYear(URI selfUriBase, String requestedYear) {
        return UriWrapper.fromUri(selfUriBase).addChild(identifier, requestedYear).getUri();
    }

    private record Issn(String value) {

    }

    private record IsbnPrefix(String value) {

    }
}
