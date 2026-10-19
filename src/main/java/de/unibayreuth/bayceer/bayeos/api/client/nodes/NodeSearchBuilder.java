package de.unibayreuth.bayceer.bayeos.api.client.nodes;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.NodeKind;
import de.unibayreuth.bayceer.bayeos.api.client.model.NodeSearchResult;

/**
 * Fluent builder for {@code GET /nodes/search}: matches full node paths against
 * an Ant-style glob ({@code ?}, {@code *}, {@code **}), optionally filtered by
 * kind and paged via {@code skip}/{@code limit}.
 */
public final class NodeSearchBuilder {

    private final HttpTransport http;
    private String pattern;
    private String kind;
    private Integer skip;
    private Integer limit;

    NodeSearchBuilder(HttpTransport http) {
        this.http = http;
    }

    /** Ant path pattern, e.g. {@code ~/**}, {@code /BayEOS/**}{@code /Temperature*}. */
    public NodeSearchBuilder pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /** Restricts the search to a node kind. */
    public NodeSearchBuilder kind(NodeKind kind) {
        this.kind = kind.value();
        return this;
    }

    /** Restricts the search to a node kind given as a raw string. */
    public NodeSearchBuilder kind(String kind) {
        this.kind = kind;
        return this;
    }

    /** Number of rows to skip (paging). */
    public NodeSearchBuilder skip(int skip) {
        this.skip = skip;
        return this;
    }

    /** Maximum number of rows to return (server default 10). */
    public NodeSearchBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public NodeSearchResult execute() {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalStateException("pattern is required");
        }
        Request request = Request.get("/nodes/search")
                .query("pattern", pattern)
                .query("kind", kind)
                .query("skip", skip)
                .query("limit", limit);
        return http.execute(request, NodeSearchResult.class);
    }
}
