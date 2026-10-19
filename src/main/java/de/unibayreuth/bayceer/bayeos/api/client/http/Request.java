package de.unibayreuth.bayceer.bayeos.api.client.http;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Internal description of a single REST call. Instances are created by the
 * fluent clients and executed by {@link HttpTransport}.
 */
public final class Request {

    public String method;
    public String path;
    public final Map<String, String> query = new LinkedHashMap<>();
    public Object body;
    /** Whether the auth header (Bearer or Node-Token) is attached. */
    public boolean auth = true;
    /** Prevents a 401-triggered refresh/retry (used by the refresh call itself). */
    public boolean noAuthRetry = false;

    private Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public static Request get(String path) {
        return new Request("GET", path);
    }

    public static Request post(String path, Object body) {
        Request r = new Request("POST", path);
        r.body = body;
        return r;
    }

    public static Request put(String path, Object body) {
        Request r = new Request("PUT", path);
        r.body = body;
        return r;
    }

    public static Request patch(String path, Object body) {
        Request r = new Request("PATCH", path);
        r.body = body;
        return r;
    }

    public static Request delete(String path) {
        return new Request("DELETE", path);
    }

    /** Adds a query parameter unless {@code value} is {@code null}. */
    public Request query(String key, Object value) {
        if (value != null) {
            query.put(key, String.valueOf(value));
        }
        return this;
    }

    public Request auth(boolean auth) {
        this.auth = auth;
        return this;
    }

    public Request noAuthRetry(boolean noAuthRetry) {
        this.noAuthRetry = noAuthRetry;
        return this;
    }
}
