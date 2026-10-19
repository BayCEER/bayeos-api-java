package de.unibayreuth.bayceer.bayeos.api.client;

import de.unibayreuth.bayceer.bayeos.api.client.auth.AuthSession;
import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.BuildInfo;
import de.unibayreuth.bayceer.bayeos.api.client.nodes.NodesClient;
import de.unibayreuth.bayceer.bayeos.api.client.timeseries.TimeseriesClient;

/**
 * Entry point of the BayEOS Java client.
 *
 * <p>
 * Create an instance with {@link #builder()}, then use the fluent sub-clients
 * {@link #nodes()} and {@link #timeseries()}. The client is
 * {@link AutoCloseable} and can be used as a try-with-resources resource;
 * instances are safe for concurrent use (the auth token is accessed
 * atomically), so a single client can be shared across threads.
 * </p>
 *
 * <pre>{@code
 * try (BayeosClient client = BayeosClient.builder().baseUrl("http://localhost:5532/bayeos-api")
 *         .credentials("root", "bayeos").build()) {
 *     client.nodes().list();
 *     client.timeseries().writeBatch().series(6).point(Instant.now(), 12.4).send();
 * }
 * }</pre>
 */
public final class BayeosClient implements AutoCloseable {

    private final HttpTransport http;
    private final AuthSession auth;
    private volatile boolean closed;

    BayeosClient(HttpTransport http, AuthSession auth) {
        this.http = http;
        this.auth = auth;
    }

    /** Creates a new client builder. */
    public static BayeosClientBuilder builder() {
        return new BayeosClientBuilder();
    }

    /** Fluent access to the node endpoints. */
    public NodesClient nodes() {
        return new NodesClient(http);
    }

    /** Fluent access to the timeseries endpoints. */
    public TimeseriesClient timeseries() {
        return new TimeseriesClient(http);
    }

    /** Returns the API build information ({@code GET /version}, public). */
    public BuildInfo version() {
        return http.execute(Request.get("/version").auth(false), BuildInfo.class);
    }

    /** Signs in with a username and password and stores the resulting JWT. */
    public BayeosClient signIn(String username, String password) {
        auth.signIn(username, password);
        return this;
    }

    /** Requests a new token for the currently authenticated user. */
    public BayeosClient refreshToken() {
        auth.refresh();
        return this;
    }

    /** Clears the local credential state (best-effort server sign-out). */
    public BayeosClient signOut() {
        auth.signOut();
        return this;
    }

    /** The current bearer token, or {@code null} in node-token mode. */
    public String token() {
        return auth.getToken();
    }

    /** The configured node token, or {@code null} in user mode. */
    public String nodeToken() {
        return auth.getNodeToken();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        auth.clear();
    }
}
