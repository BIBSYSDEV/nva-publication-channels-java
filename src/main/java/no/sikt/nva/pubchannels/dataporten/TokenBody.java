package no.sikt.nva.pubchannels.dataporten;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenBody {
    @JsonProperty("access_token")
    private final String accessToken;
    @JsonProperty("token_type")
    private final String tokenType;

    @JsonCreator
    public TokenBody(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType
    ) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
