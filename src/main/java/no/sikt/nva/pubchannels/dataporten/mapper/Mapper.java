package no.sikt.nva.pubchannels.dataporten.mapper;

public interface Mapper {
    <E extends Enum<E>> E map(String value);
}
