package no.sikt.nva.pubchannels.channelregistry;

import java.util.UUID;
import no.unit.nva.commons.json.JsonSerializable;

public record ChannelRegistryUpdateChannelRequest(
    UUID pid, String name, String pissn, String eissn, String isbnPrefix, String type)
    implements JsonSerializable {}
