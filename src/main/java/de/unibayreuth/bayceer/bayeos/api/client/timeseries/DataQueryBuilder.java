package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.BatchData;
import de.unibayreuth.bayceer.bayeos.api.client.model.SampleFunc;
import de.unibayreuth.bayceer.bayeos.api.client.model.SampleInterval;
import de.unibayreuth.bayceer.bayeos.api.client.model.Status;
import de.unibayreuth.bayceer.bayeos.api.client.model.TsType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fluent builder for reading a single series ({@code GET /timeseries/{id}/data}).
 */
public final class DataQueryBuilder {

    private final HttpTransport http;
    private final long id;

    private Instant from;
    private Instant to;
    private String sampleInterval;
    private String sampleFunc;
    private Integer limit;
    private String tsType;
    private List<Status> statusFilter;

    DataQueryBuilder(HttpTransport http, long id) {
        this.http = http;
        this.id = id;
    }

    public DataQueryBuilder from(Instant from) {
        this.from = from;
        return this;
    }

    public DataQueryBuilder to(Instant to) {
        this.to = to;
        return this;
    }

    public DataQueryBuilder sampleInterval(SampleInterval interval) {
        this.sampleInterval = interval.code();
        return this;
    }

    public DataQueryBuilder sampleFunc(SampleFunc func) {
        this.sampleFunc = func.code();
        return this;
    }

    public DataQueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public DataQueryBuilder tsType(TsType tsType) {
        this.tsType = tsType.code();
        return this;
    }

    public DataQueryBuilder statusFilter(Status... statuses) {
        this.statusFilter = List.of(statuses);
        return this;
    }

    public DataQueryBuilder statusFilter(Collection<Status> statuses) {
        this.statusFilter = List.copyOf(statuses);
        return this;
    }

    public BatchData execute() {
        Request request = Request.get("/timeseries/" + id + "/data")
                .query("from", Times.iso(from))
                .query("to", Times.iso(to))
                .query("sample_interval", sampleInterval)
                .query("sample_func", sampleFunc)
                .query("limit", limit)
                .query("ts_type", tsType)
                .query("status_filter", csv(statusFilter));
        return http.execute(request, BatchData.class);
    }

    static String csv(List<Status> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return statuses.stream().map(s -> Integer.toString(s.code())).collect(Collectors.joining(","));
    }
}
