package de.unibayreuth.bayceer.bayeos.api.client.auth;

import com.fasterxml.jackson.databind.JsonNode;
import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Json;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.TokenResponse;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the credentials of a client and resolves the auth header for outgoing
 * requests. Supports two mutually exclusive modes:
 *
 * <ul>
 *   <li><b>User / JWT</b>: {@link #signIn(String, String)} obtains and stores a
 *       bearer token; the token can be refreshed and, optionally, re-issued
 *       automatically on expiry or after a 401.</li>
 *   <li><b>Node-Token</b>: a pre-issued UUID is sent in the {@code Node-Token}
 *       header, typically for devices/loggers.</li>
 * </ul>
 */
public final class AuthSession {

    private static final long REFRESH_MARGIN_MILLIS = 10_000L;

    private final HttpTransport http;

    private volatile String bearerToken;
    private volatile String nodeToken;
    private volatile String username;
    private volatile String password;
    private volatile boolean autoRefresh;
    private volatile long expiresAtMillis = -1L;

    public AuthSession(HttpTransport http) {
        this.http = http;
    }

    public void setToken(String token) {
        this.bearerToken = token;
        this.expiresAtMillis = parseExpiry(token);
    }

    public void setNodeToken(String nodeToken) {
        this.nodeToken = nodeToken;
    }

    public void setCredentials(String username, String password, boolean autoRefresh) {
        this.username = username;
        this.password = password;
        this.autoRefresh = autoRefresh;
    }

    public String getToken() {
        return bearerToken;
    }

    public String getNodeToken() {
        return nodeToken;
    }

    public boolean hasToken() {
        return bearerToken != null;
    }

    /** Signs in with explicit credentials and stores the resulting token. */
    public void signIn(String username, String password) {
        this.username = username;
        this.password = password;
        TokenResponse response = http.execute(
                Request.post("/auth/signin", credentialsBody(username, password)).auth(false),
                TokenResponse.class);
        if (response == null || response.getToken() == null) {
            throw new IllegalStateException("Sign-in response did not contain a token");
        }
        setToken(response.getToken());
    }

    /** Requests a new token for the currently authenticated user. */
    public void refresh() {
        TokenResponse response = http.execute(
                Request.put("/auth/refresh", null).noAuthRetry(true),
                TokenResponse.class);
        if (response == null || response.getToken() == null) {
            throw new IllegalStateException("Refresh response did not contain a token");
        }
        setToken(response.getToken());
    }

    /**
     * Clears the local credential state. When a username/password is known the
     * {@code POST /auth/signout} endpoint is called on a best-effort basis.
     */
    public void signOut() {
        if (bearerToken != null && username != null && password != null) {
            try {
                http.execute(Request.post("/auth/signout", credentialsBody(username, password)));
            } catch (RuntimeException ignored) {
                // signout is best-effort; local state is cleared regardless
            }
        }
        clear();
    }

    /** Clears all local credential state without contacting the server. */
    public void clear() {
        this.bearerToken = null;
        this.expiresAtMillis = -1L;
    }

    public void ensureToken() {
        if (bearerToken == null && nodeToken == null && username != null && password != null) {
            signIn(username, password);
            return;
        }
        if (autoRefresh && bearerToken != null && username != null && isExpiringSoon()) {
            refresh();
        }
    }

    public boolean canRefresh() {
        return nodeToken == null && username != null && password != null;
    }

    public void applyHeaders(HttpRequest.Builder builder) {
        if (nodeToken != null) {
            builder.header("Node-Token", nodeToken);
        } else if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
    }

    private boolean isExpiringSoon() {
        return expiresAtMillis > 0
                && System.currentTimeMillis() >= expiresAtMillis - REFRESH_MARGIN_MILLIS;
    }

    private static Map<String, Object> credentialsBody(String username, String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", username);
        body.put("password", password);
        return body;
    }

    /**
     * Best-effort extraction of the {@code exp} claim (seconds since epoch) from
     * an unverified JWT payload. Returns {@code -1} when unavailable.
     */
    static long parseExpiry(String token) {
        if (token == null) {
            return -1L;
        }
        int firstDot = token.indexOf('.');
        int secondDot = token.indexOf('.', firstDot + 1);
        if (firstDot < 0 || secondDot < 0) {
            return -1L;
        }
        try {
            String payload = token.substring(firstDot + 1, secondDot);
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            JsonNode node = Json.mapper().readTree(new String(decoded, StandardCharsets.UTF_8));
            JsonNode exp = node.get("exp");
            return exp != null && exp.isNumber() ? exp.asLong() * 1000L : -1L;
        } catch (RuntimeException | java.io.IOException e) {
            return -1L;
        }
    }
}
