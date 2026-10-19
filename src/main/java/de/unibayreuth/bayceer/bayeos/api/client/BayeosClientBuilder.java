package de.unibayreuth.bayceer.bayeos.api.client;

import de.unibayreuth.bayceer.bayeos.api.client.auth.AuthSession;
import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Builder for {@link BayeosClient}.
 *
 * <p>Authentication is either credential-based (username/password, JWT bearer)
 * or node-token based. The two are mutually exclusive. With credentials, the
 * sign-in is performed lazily on the first secured request unless a token is
 * supplied directly.</p>
 */
public final class BayeosClientBuilder {

    private String baseUrl;
    private String contextPath;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration requestTimeout = Duration.ofSeconds(60);
    private String token;
    private String nodeToken;
    private String username;
    private String password;
    private boolean autoRefresh;

    BayeosClientBuilder() {
    }

    /**
     * Base URL of the API, including the API context, e.g.
     * {@code http://localhost:5532/bayeos-api}. A trailing slash is ignored.
     */
    public BayeosClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /** Appends an API context path, e.g. {@code /bayeos-api}. */
    public BayeosClientBuilder contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public BayeosClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public BayeosClientBuilder requestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /** Supplies a pre-issued bearer token instead of signing in. */
    public BayeosClientBuilder token(String token) {
        this.token = token;
        return this;
    }

    /** Uses node-token authentication (device/logger mode). */
    public BayeosClientBuilder nodeToken(String nodeToken) {
        this.nodeToken = nodeToken;
        return this;
    }

    /**
     * Uses username/password authentication. The sign-in happens lazily on the
     * first secured request.
     */
    public BayeosClientBuilder credentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * When {@code true}, an expired token is refreshed before a request and a
     * 401 response triggers a one-shot refresh/retry (requires credentials).
     */
    public BayeosClientBuilder autoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
        return this;
    }

    public BayeosClient build() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalStateException("baseUrl is required");
        }
        if (nodeToken != null && (username != null || token != null)) {
            throw new IllegalStateException("nodeToken cannot be combined with credentials or token");
        }

        String resolvedBase = baseUrl;
        if (contextPath != null && !contextPath.isEmpty()) {
            resolvedBase = resolvedBase + (contextPath.startsWith("/") ? contextPath : "/" + contextPath);
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpTransport transport = new HttpTransport(resolvedBase, httpClient, requestTimeout);
        AuthSession session = new AuthSession(transport);
        transport.setAuth(session);

        if (token != null) {
            session.setToken(token);
        }
        if (nodeToken != null) {
            session.setNodeToken(nodeToken);
        }
        if (username != null) {
            session.setCredentials(username, password, autoRefresh);
        }

        return new BayeosClient(transport, session);
    }
}
