package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body of the sign-in and token-refresh endpoints: {@code { token }}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TokenResponse {

    @JsonProperty("token")
    private String token;

    TokenResponse() {
    }

    public String getToken() {
        return token;
    }
}
