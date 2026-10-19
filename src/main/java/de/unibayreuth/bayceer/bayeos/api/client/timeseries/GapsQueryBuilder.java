package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.GapInfo;

import java.time.Instant;
import java.util.List;

/**
 * Fluent builder for {@code GET /timeseries/{id}/gaps}.
 */
public final class GapsQueryBuilder {

    private final HttpTransport http;
    private final long id;

    private Instant from;
    private Instant to;
    private Integer thresholdMinutes;
    private Integer limit;

    GapsQueryBuilder(HttpTransport http, long id) {
        this.http = http;
        this.id = id;
    }

    public GapsQueryBuilder from(Instant from) {
        this.from = from;
        return this;
    }

    public GapsQueryBuilder to(Instant to) {
        this.to = to;
        return this;
    }

    /**
     * Overrides the gap threshold derived from the node's resolution metadata.
     * When set, the node's resolution is not required.
     */
    public GapsQueryBuilder thresholdMinutes(int thresholdMinutes) {
        this.thresholdMinutes = thresholdMinutes;
        return this;
    }

    /** Caps the number of gaps returned (longest first; server default 20). */
    public GapsQueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public List<GapInfo> execute() {
        Request request = Request.get("/timeseries/" + id + "/gaps")
                .query("from", Times.iso(from))
                .query("to", Times.iso(to))
                .query("threshold_minutes", thresholdMinutes)
                .query("limit", limit);
        return http.executeList(request, GapInfo.class);
    }
}
