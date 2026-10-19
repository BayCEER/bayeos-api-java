package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.BatchData;
import de.unibayreuth.bayceer.bayeos.api.client.model.GapInfo;
import de.unibayreuth.bayceer.bayeos.api.client.model.Node;
import de.unibayreuth.bayceer.bayeos.api.client.model.TimeseriesStats;
import de.unibayreuth.bayceer.bayeos.api.client.nodes.NodesClient;

import java.util.List;
import java.util.Map;

/**
 * Fluent access to the {@code /timeseries} endpoints: create/manage timeseries,
 * write and read data (single series or batched across several).
 */
public final class TimeseriesClient {

    private final HttpTransport http;

    public TimeseriesClient(HttpTransport http) {
        this.http = http;
    }

    /** Fetches a timeseries node by id ({@code GET /timeseries/{id}}). */
    public Node get(long id) {
        return http.execute(Request.get("/timeseries/" + id), Node.class);
    }

    /**
     * Lists timeseries nodes by id. The API exposes this through the node
     * collection ({@code GET /nodes?ids=…}); filter by
     * {@link Node#getKind()} when needed.
     */
    public List<Node> list(long... ids) {
        return new NodesClient(http).list(ids);
    }

    /** Starts creating a timeseries ({@code POST /timeseries}). */
    public TimeseriesCreateBuilder create(String displayName) {
        return new TimeseriesCreateBuilder(http, displayName);
    }

    /** Updates arbitrary fields of a timeseries ({@code PATCH /timeseries/{id}}). */
    public void update(long id, Map<String, Object> fields) {
        http.execute(Request.patch("/timeseries/" + id, fields));
    }

    /** Deletes a timeseries ({@code DELETE /timeseries/{id}}). */
    public void delete(long id) {
        http.execute(Request.delete("/timeseries/" + id));
    }

    /** Starts writing points to a single series ({@code POST /timeseries/{id}/data}). */
    public DataWriteBuilder write(long id) {
        return new DataWriteBuilder(http, id);
    }

    /** Starts a batched write across several series ({@code POST /timeseries/data}). */
    public DataBatchBuilder writeBatch() {
        return new DataBatchBuilder(http);
    }

    /** Starts reading a single series ({@code GET /timeseries/{id}/data}). */
    public DataQueryBuilder read(long id) {
        return new DataQueryBuilder(http, id);
    }

    /** Starts reading several series in one request ({@code GET /timeseries/data}). */
    public BatchDataQueryBuilder readBatch() {
        return new BatchDataQueryBuilder(http);
    }

    /** Starts a statistics query ({@code GET /timeseries/{id}/stats}). */
    public StatsQueryBuilder stats(long id) {
        return new StatsQueryBuilder(http, id);
    }

    /** Starts a gap-detection query ({@code GET /timeseries/{id}/gaps}). */
    public GapsQueryBuilder gaps(long id) {
        return new GapsQueryBuilder(http, id);
    }

    /** Convenience: executes a {@link StatsQueryBuilder} without further filters. */
    public TimeseriesStats statsNow(long id) {
        return stats(id).execute();
    }

    /** Convenience: executes a {@link GapsQueryBuilder} without further filters. */
    public List<GapInfo> gapsNow(long id) {
        return gaps(id).execute();
    }
}
