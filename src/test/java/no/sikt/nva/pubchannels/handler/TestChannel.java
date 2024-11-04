package no.sikt.nva.pubchannels.handler;

import static java.util.Objects.isNull;
import static no.sikt.nva.pubchannels.handler.TestUtils.scientificValueToLevel;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import java.net.URI;
import java.util.Map;
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
                          URI sameAs,
                          ScientificValueReviewNotice reviewNotice) {

    private static final String CHANNEL_REGISTRY_REVIEW_NOTICE_MARK = "X";

    public TestChannel(Integer year, String identifier) {
        this(identifier, year, randomString(), randomElement(ScientificValue.values()), new IsbnPrefix(randomString()),
             new Issn(randomIssn()), new Issn(randomIssn()), randomString(), randomUri(), null);
    }

    public TestChannel withName(String name) {
        return new TestChannel(identifier, year, name, scientificValue, isbnPrefix, onlineIssn, printIssn, discontinued,
                               sameAs, reviewNotice);
    }

    public TestChannel withPrintIssn(String printIssn) {
        return new TestChannel(identifier, year, name, scientificValue, isbnPrefix, onlineIssn, new Issn(printIssn),
                               discontinued, sameAs, reviewNotice);
    }

    public TestChannel withScientificValueReviewNotice(Map<String, String> comment) {
        return new TestChannel(identifier, year, name, scientificValue, isbnPrefix, onlineIssn, printIssn,
                               discontinued, sameAs, new ScientificValueReviewNotice(comment));
    }

    public String asChannelRegistryJournalBodyWithoutLevel() {
        return new ChannelRegistryJournal(identifier,
                                          name,
                                          onlineIssn.value(),
                                          printIssn.value(),
                                          null,
                                          sameAs,
                                          discontinued)
                   .toJsonString();
    }

    public String asChannelRegistryJournalBody() {
        return new ChannelRegistryJournal(identifier,
                                          name,
                                          onlineIssn.value(),
                                          printIssn.value(),
                                          mapValuesToChannelRegistryLevel(),
                                          sameAs,
                                          discontinued)
                   .toJsonString();
    }

    public String asChannelRegistrySeriesBody() {
        return new ChannelRegistrySeries(identifier,
                                         name,
                                         onlineIssn.value(),
                                         printIssn.value(),
                                         mapValuesToChannelRegistryLevel(),
                                         sameAs,
                                         discontinued)
                   .toJsonString();
    }

    public String asChannelRegistryPublisherBody() {
        return new ChannelRegistryPublisher(identifier,
                                            mapValuesToChannelRegistryLevel(),
                                            isbnPrefix.value(),
                                            name,
                                            sameAs,
                                            discontinued)
                   .toJsonString();
    }

    public JournalDto asJournalDto(URI selfUriBase, String requestedYear) {
        var id = generateIdWithYear(selfUriBase, requestedYear);
        return new JournalDto(id, identifier, name, onlineIssn.value(), printIssn.value(),
                              scientificValue, sameAs, discontinued, requestedYear, reviewNotice);
    }

    public SeriesDto asSeriesDto(String selfUriBase, String requestedYear) {
        var id = generateIdWithYear(URI.create(selfUriBase), requestedYear);
        return new SeriesDto(id, identifier, name, onlineIssn.value(), printIssn.value(), scientificValue,
                             sameAs, discontinued, requestedYear, reviewNotice);
    }

    public PublisherDto asPublisherDto(String selfUriBase, String requestedYear) {
        var id = generateIdWithYear(URI.create(selfUriBase), requestedYear);
        return new PublisherDto(id, identifier, name, isbnPrefix.value(), scientificValue, sameAs, discontinued,
                                requestedYear, reviewNotice);
    }

    public String getIdentifier() {
        return identifier;
    }

    private URI generateIdWithYear(URI selfUriBase, String requestedYear) {
        return UriWrapper.fromUri(selfUriBase).addChild(identifier, requestedYear).getUri();
    }

    private ChannelRegistryLevel mapValuesToChannelRegistryLevel() {
        String level = scientificValueToLevel(scientificValue);
        return new ChannelRegistryLevel(year,
                                        level,
                                        isNull(reviewNotice) ? level
                                            : CHANNEL_REGISTRY_REVIEW_NOTICE_MARK,
                                        isNull(reviewNotice) ? null
                                            : reviewNotice.comments().get("no"),
                                        isNull(reviewNotice) ? null
                                            : reviewNotice.comments().get("en"));
    }

    private record Issn(String value) {

    }

    private record IsbnPrefix(String value) {

    }
}
