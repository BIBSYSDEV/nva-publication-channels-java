package no.sikt.nva.pubchannels.channelregistry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.unit.nva.commons.json.JsonSerializable;

public record ChannelRegistryUpdateChannelRequest(Fields fields, @JsonIgnore String type)
    implements JsonSerializable {

  public record Fields(String pid, String name, String pissn, String eissn, String isbnPrefix) {}
}
