package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.SampleInterval;
import de.unibayreuth.bayceer.bayeos.api.client.model.TimeseriesStats;

import java.time.Instant;

/**
 * Fluent builder for {@code GET /timeseries/{id}/stats}.
 */
public final class StatsQueryBuilder {

    private final HttpTransport http;
    private final long id;

    private Instant from;
    private Instant to;
    private String agg;

    StatsQueryBuilder(HttpTransport http, long id) {
        this.http = http;
        this.id = id;
    }

    public StatsQueryBuilder from(Instant from) {
        this.from = from;
        return this;
    }

    public StatsQueryBuilder to(Instant to) {
        this.to = to;
        return this;
    }

    /** Sets the rollup granularity. */
    public StatsQueryBuilder agg(SampleInterval agg) {
        this.agg = agg.code();
        return this;
    }

    public TimeseriesStats execute() {
        Request request = Request.get("/timeseries/" + id + "/stats")
                .query("from", Times.iso(from))
                .query("to", Times.iso(to))
                .query("agg", agg);
        return http.execute(request, TimeseriesStats.class);
    }
}
