package no.sikt.nva.pubchannels.handler;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;

@JsonSerialize
@JsonTypeInfo(use = Id.NAME, property = "type")
public record ScientificValueReviewNotice(Map<String, String> comments) {

}
