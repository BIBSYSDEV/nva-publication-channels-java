package no.sikt.nva.pubchannels.channelregistry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import no.unit.nva.commons.json.JsonSerializable;

public record ChannelRegistryUpdateChannelRequest(
    UUID pid, String name, String pissn, String eissn, String isbnPrefix, @JsonIgnore String type)
    implements JsonSerializable {}
