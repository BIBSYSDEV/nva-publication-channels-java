package no.sikt.nva.pubchannels.handler;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.handler.ScientificValue.UNASSIGNED;
import static no.sikt.nva.pubchannels.handler.TestUtils.scientificValueToLevel;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import java.net.URI;
import java.util.Map;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryLevel;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySerialPublication;
import no.sikt.nva.pubchannels.handler.model.PublisherDto;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import nva.commons.core.paths.UriWrapper;

public record TestChannel(
    String identifier,
    Integer year,
    String name,
    ScientificValue scientificValue,
    IsbnPrefix isbnPrefix,
    Issn onlineIssn,
    Issn printIssn,
    String discontinued,
    URI sameAs,
    ScientificValueReviewNotice reviewNotice,
    String type) {

    private static final String CHANNEL_REGISTRY_REVIEW_NOTICE_MARK = "X";

    public TestChannel(String year, String identifier, String type) {
        this(
            identifier,
            nonNull(year) ? Integer.parseInt(year) : null,
            randomString(),
            randomElement(ScientificValue.values()),
            new IsbnPrefix(randomString()),
            new Issn(randomIssn()),
            new Issn(randomIssn()),
            randomString(),
            randomUri(),
            null,
            type);
    }

    public static TestChannel createEmptyTestChannel(Integer year, String identifier, String type) {
        return new TestChannel(
            identifier, year, null, UNASSIGNED, null, null, null, null, null, null, type);
    }

    public TestChannel withName(String name) {
        return new TestChannel(
            identifier,
            year,
            name,
            scientificValue,
            isbnPrefix,
            onlineIssn,
            printIssn,
            discontinued,
            sameAs,
            reviewNotice,
            type);
    }

    public TestChannel withOnlineIssn(String onlineIssn) {
        return new TestChannel(
            identifier,
            year,
            name,
            scientificValue,
            isbnPrefix,
            new Issn(onlineIssn),
            printIssn,
            discontinued,
            sameAs,
            reviewNotice,
            type);
    }

    public TestChannel withPrintIssn(String printIssn) {
        return new TestChannel(
            identifier,
            year,
            name,
            scientificValue,
            isbnPrefix,
            onlineIssn,
            new Issn(printIssn),
            discontinued,
            sameAs,
            reviewNotice,
            type);
    }

    public TestChannel withSameAs(URI sameAs) {
        return new TestChannel(
            identifier,
            year,
            name,
            scientificValue,
            isbnPrefix,
            onlineIssn,
            printIssn,
            discontinued,
            sameAs,
            reviewNotice,
            type);
    }

    public TestChannel withScientificValueReviewNotice(Map<String, String> comment) {
        return new TestChannel(
            identifier,
            year,
            name,
            scientificValue,
            isbnPrefix,
            onlineIssn,
            printIssn,
            discontinued,
            sameAs,
            new ScientificValueReviewNotice(comment),
            type);
    }

    public String asChannelRegistryResponseBody() {
        return switch (type) {
            case "Publisher" -> asChannelRegistryPublisherBody();
            case "Series", "Journal" -> asChannelRegistrySerialPublicationBody();
            default -> throw new IllegalArgumentException(
                "Unknown type. Expected one of [Publisher, Series, Journal]");
        };
    }

    public String asChannelRegistrySerialPublicationBody() {
        return new ChannelRegistrySerialPublication(
            identifier,
            name,
            getOnlineIssnValue(),
            getPrintIssnValue(),
            mapValuesToChannelRegistryLevel(),
            sameAs,
            discontinued,
            type)
                   .toJsonString();
    }

    public String asChannelRegistryPublisherBody() {
        return new ChannelRegistryPublisher(
            identifier,
            mapValuesToChannelRegistryLevel(),
            getIsbnPrefix(),
            name,
            sameAs,
            discontinued,
            type)
                   .toJsonString();
    }

    public String asChannelRegistryPublisherBodyWithoutLevel() {
        return new ChannelRegistryPublisher(
            identifier, null, getIsbnPrefix(), name, sameAs, discontinued, type)
                   .toJsonString();
    }

    public SerialPublicationDto asSerialPublicationDto(URI selfUriBase, String requestedYear) {
        var id = generateIdWithYear(selfUriBase, requestedYear);
        return new SerialPublicationDto(
            id,
            identifier,
            name,
            getOnlineIssnValue(),
            getPrintIssnValue(),
            scientificValue,
            sameAs,
            discontinued,
            type,
            requestedYear,
            reviewNotice);
    }

    public PublisherDto asPublisherDto(URI selfUriBase, String requestedYear) {
        var id = generateIdWithYear(selfUriBase, requestedYear);
        return new PublisherDto(
            id,
            identifier,
            name,
            getIsbnPrefix(),
            scientificValue,
            sameAs,
            discontinued,
            requestedYear,
            reviewNotice);
    }

    public String asChannelRegistrySeriesBodyWithoutLevel() {
        return new ChannelRegistrySerialPublication(
            identifier,
            name,
            getOnlineIssnValue(),
            getPrintIssnValue(),
            null,
            sameAs,
            discontinued,
            type)
                   .toJsonString();
    }

    private URI generateIdWithYear(URI selfUriBase, String requestedYear) {
        return UriWrapper
                   .fromUri(selfUriBase)
                   .addChild(identifier, requestedYear)
                   .getUri();
    }

    private ChannelRegistryLevel mapValuesToChannelRegistryLevel() {
        var level = scientificValueToLevel(scientificValue);
        return new ChannelRegistryLevel(
            year,
            level,
            isNull(reviewNotice) ? level : CHANNEL_REGISTRY_REVIEW_NOTICE_MARK,
            isNull(reviewNotice) ? null : reviewNotice
                                              .comments()
                                              .get("no"),
            isNull(reviewNotice) ? null : reviewNotice
                                              .comments()
                                              .get("en"));
    }

    public String getOnlineIssnValue() {
        return nonNull(onlineIssn) ? onlineIssn.value() : null;
    }

    public String getPrintIssnValue() {
        return nonNull(printIssn) ? printIssn.value() : null;
    }

    public String getIsbnPrefix() {
        return nonNull(isbnPrefix) ? isbnPrefix.value() : null;
    }

    private record Issn(String value) {

    }

    private record IsbnPrefix(String value) {

    }
}
