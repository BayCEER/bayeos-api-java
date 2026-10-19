package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.BatchData;
import de.unibayreuth.bayceer.bayeos.api.client.model.SampleFunc;
import de.unibayreuth.bayceer.bayeos.api.client.model.SampleInterval;
import de.unibayreuth.bayceer.bayeos.api.client.model.Status;
import de.unibayreuth.bayceer.bayeos.api.client.model.TsType;
import de.unibayreuth.bayceer.bayeos.api.client.nodes.NodesClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Fluent builder for reading several series in one request
 * ({@code GET /timeseries/data}).
 */
public final class BatchDataQueryBuilder {

    private final HttpTransport http;
    private final List<Long> ids = new ArrayList<>();

    private Instant from;
    private Instant to;
    private String sampleInterval;
    private String sampleFunc;
    private String tsType;
    private List<Status> statusFilter;

    BatchDataQueryBuilder(HttpTransport http) {
        this.http = http;
    }

    public BatchDataQueryBuilder ids(long... ids) {
        Arrays.stream(ids).forEach(this.ids::add);
        return this;
    }

    public BatchDataQueryBuilder ids(Collection<Long> ids) {
        this.ids.addAll(ids);
        return this;
    }

    public BatchDataQueryBuilder from(Instant from) {
        this.from = from;
        return this;
    }

    public BatchDataQueryBuilder to(Instant to) {
        this.to = to;
        return this;
    }

    public BatchDataQueryBuilder sampleInterval(SampleInterval interval) {
        this.sampleInterval = interval.code();
        return this;
    }

    public BatchDataQueryBuilder sampleFunc(SampleFunc func) {
        this.sampleFunc = func.code();
        return this;
    }

    public BatchDataQueryBuilder tsType(TsType tsType) {
        this.tsType = tsType.code();
        return this;
    }

    public BatchDataQueryBuilder statusFilter(Status... statuses) {
        this.statusFilter = List.of(statuses);
        return this;
    }

    public BatchDataQueryBuilder statusFilter(Collection<Status> statuses) {
        this.statusFilter = List.copyOf(statuses);
        return this;
    }

    public BatchData execute() {
        if (ids.isEmpty()) {
            throw new IllegalStateException("At least one id is required");
        }
        Request request = Request.get("/timeseries/data")
                .query("ids", joinIds())
                .query("from", Times.iso(from))
                .query("to", Times.iso(to))
                .query("sample_interval", sampleInterval)
                .query("sample_func", sampleFunc)
                .query("ts_type", tsType)
                .query("status_filter", DataQueryBuilder.csv(statusFilter));
        return http.execute(request, BatchData.class);
    }

    private String joinIds() {
        long[] raw = new long[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            raw[i] = ids.get(i);
        }
        return NodesClient.joinIds(raw);
    }
}
