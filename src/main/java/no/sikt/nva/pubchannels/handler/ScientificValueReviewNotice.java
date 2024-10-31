package no.sikt.nva.pubchannels.handler;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;

@JsonSerialize
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record ScientificValueReviewNotice(Map<String, String> comment) {

}
