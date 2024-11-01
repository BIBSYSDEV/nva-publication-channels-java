package no.sikt.nva.pubchannels.handler;

import static java.util.Objects.isNull;
import static no.sikt.nva.pubchannels.handler.TestUtils.scientificValueToLevel;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.attempt.Try.attempt;
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

public class TestChannel {

    private static final String CHANNEL_REGISTRY_REVIEW_NOTICE_MARK = "X";
    private final String identifier;
    private final Integer year;
    private final String isbnPrefix;
    private final ScientificValue scientificValue;
    private final String discontinued;
    private final URI sameAs;
    private final String onlineIssn;
    private final ScientificValueReviewNotice reviewNotice;
    private String name;
    private String printIssn;

    private TestChannel(Integer year, String identifier, String name,
                        String onlineIssn, String printIssn, String isbnPrefix,
                        ScientificValue scientificValue,
                        String discontinued, URI sameAs, ScientificValueReviewNotice reviewNotice) {

        this.identifier = identifier;
        this.year = year;
        this.name = name;
        this.onlineIssn = onlineIssn;
        this.printIssn = printIssn;
        this.isbnPrefix = isbnPrefix;
        this.scientificValue = scientificValue;
        this.discontinued = discontinued;
        this.sameAs = sameAs;
        this.reviewNotice = reviewNotice;
    }

    public TestChannel(Integer year, String identifier) {
        this(year, identifier, randomString(), randomIssn(), randomIssn(), randomString(),
             randomElement(ScientificValue.values()), randomString(), randomUri(),
             null);
    }

    public TestChannel withName(String name) {
        this.name = name;
        return this;
    }

    public TestChannel withPrintIssn(String printIssn) {
        this.printIssn = printIssn;
        return this;
    }

    public TestChannel withScientificValueReviewNotice(Map<String, String> comment) {
        return new TestChannel(year, identifier, name, onlineIssn, printIssn, isbnPrefix, scientificValue,
                               discontinued, sameAs, new ScientificValueReviewNotice(comment));
    }

    public String asChannelRegistryJournalBody() {
        var channelRegistryBody = new ChannelRegistryJournal(identifier,
                                                             name,
                                                             onlineIssn,
                                                             printIssn,
                                                             mapValuesToChannelRegistryLevel(),
                                                             sameAs,
                                                             discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public String asChannelRegistrySeriesBody() {
        var channelRegistryBody = new ChannelRegistrySeries(identifier, name, onlineIssn, printIssn,
                                                            mapValuesToChannelRegistryLevel(),
                                                            sameAs,
                                                            discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public String asChannelRegistryPublisherBody() {
        var channelRegistryBody = new ChannelRegistryPublisher(identifier,
                                                               mapValuesToChannelRegistryLevel(),
                                                               isbnPrefix,
                                                               name,
                                                               sameAs,
                                                               discontinued);
        return attempt(() -> dtoObjectMapper.writeValueAsString(channelRegistryBody)).orElseThrow();
    }

    public JournalDto asJournalDto(URI selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(selfUriBase, requestedYear);
        return new JournalDto(expectedId, identifier, name, onlineIssn, printIssn,
                              scientificValue, sameAs, discontinued, requestedYear, reviewNotice);
    }

    public SeriesDto asSeriesDto(String selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(URI.create(selfUriBase), requestedYear);
        return new SeriesDto(expectedId, identifier, name, onlineIssn, printIssn, scientificValue, sameAs,
                             discontinued, requestedYear, reviewNotice);
    }

    public PublisherDto asPublisherDto(String selfUriBase, String requestedYear) {
        var expectedId = generateExpectedId(URI.create(selfUriBase), requestedYear);
        return new PublisherDto(expectedId, identifier, name, isbnPrefix, scientificValue, sameAs, discontinued,
                                requestedYear, reviewNotice);
    }

    public String getIdentifier() {
        return identifier;
    }

    private ChannelRegistryLevel mapValuesToChannelRegistryLevel() {
        String level = scientificValueToLevel(scientificValue);
        return new ChannelRegistryLevel(year,
                                        level,
                                        isNull(reviewNotice) ? level
                                            : CHANNEL_REGISTRY_REVIEW_NOTICE_MARK,
                                        isNull(reviewNotice) ? null
                                            : reviewNotice.comment().get("no"),
                                        isNull(reviewNotice) ? null
                                            : reviewNotice.comment().get("en"));
    }

    private URI generateExpectedId(URI selfUriBase, String requestedYear) {
        return UriWrapper.fromUri(selfUriBase).addChild(identifier, requestedYear).getUri();
    }
}
