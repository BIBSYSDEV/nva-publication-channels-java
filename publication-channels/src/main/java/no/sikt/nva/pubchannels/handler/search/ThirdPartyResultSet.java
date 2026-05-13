package no.sikt.nva.pubchannels.handler.search;

import java.util.List;

@FunctionalInterface
public interface ThirdPartyResultSet<T> {

  List<T> pageResult();
}
