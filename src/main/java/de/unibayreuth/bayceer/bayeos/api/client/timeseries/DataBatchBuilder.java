package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.DataPoint;
import de.unibayreuth.bayceer.bayeos.api.client.model.Status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for batched writes across several series
 * ({@code POST /timeseries/data}). Points are accumulated per series and sent
 * column-oriented ({@code {series:[{id, ts[], values[], status[]?}]}}).
 *
 * <p>Large batches are split automatically: {@link #chunkSize(int)} caps the
 * number of points per series per request, so every series is written in
 * request {@code i} using its own {@code i}-th chunk of points.</p>
 *
 * <pre>{@code
 * client.timeseries().writeBatch()
 *         .series(6).point(t1, 12.4).point(t2, 12.9, Status.VALID)
 *         .series(7).points(rows)
 *         .ignoreInvalidTimestamps(true)
 *         .send();
 * }</pre>
 */
public final class DataBatchBuilder {

    private final HttpTransport http;
    private final List<MutableSeries> series = new ArrayList<>();
    private boolean ignoreInvalidTimestamps;
    private int chunkSize = 5000;

    DataBatchBuilder(HttpTransport http) {
        this.http = http;
    }

    /** Selects a series (by node id) and returns a scope to add its points. */
    public SeriesScope series(long id) {
        MutableSeries target = new MutableSeries(id);
        series.add(target);
        return new SeriesScope(this, target);
    }

    /** Caps the number of points per series per request. */
    public DataBatchBuilder chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    /** Silently ignores timestamps the server considers invalid. */
    public DataBatchBuilder ignoreInvalidTimestamps(boolean ignoreInvalidTimestamps) {
        this.ignoreInvalidTimestamps = ignoreInvalidTimestamps;
        return this;
    }

    public void send() {
        if (chunkSize <= 0) {
            throw new IllegalStateException("chunkSize must be positive");
        }
        int offset = 0;
        while (true) {
            List<Map<String, Object>> batch = new ArrayList<>();
            for (MutableSeries s : series) {
                if (s.points.isEmpty() || offset >= s.points.size()) {
                    continue;
                }
                int end = Math.min(offset + chunkSize, s.points.size());
                batch.add(toColumnSeries(s.id, s.points.subList(offset, end)));
            }
            if (batch.isEmpty()) {
                break;
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("series", batch);
            Request request = Request.post("/timeseries/data", body);
            if (ignoreInvalidTimestamps) {
                request.query("ignore_invalid_timestamps", true);
            }
            http.execute(request);
            offset += chunkSize;
        }
    }

    private static Map<String, Object> toColumnSeries(long id, List<DataPoint> points) {
        List<Instant> ts = new ArrayList<>(points.size());
        List<Double> values = new ArrayList<>(points.size());
        List<Integer> status = new ArrayList<>(points.size());
        boolean anyStatus = false;
        for (DataPoint p : points) {
            ts.add(p.getTs());
            values.add(p.getValue());
            Status s = p.getStatus();
            if (s != null) {
                anyStatus = true;
            }
            status.add(s == null ? Status.NOT_SET.code() : s.code());
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", id);
        entry.put("ts", ts);
        entry.put("values", values);
        if (anyStatus) {
            entry.put("status", status);
        }
        return entry;
    }

    /** Per-series scope that allows chaining points and returning to the parent. */
    public static final class SeriesScope {

        private final DataBatchBuilder parent;
        private final MutableSeries target;

        SeriesScope(DataBatchBuilder parent, MutableSeries target) {
            this.parent = parent;
            this.target = target;
        }

        public SeriesScope point(Instant ts, double value) {
            target.points.add(new DataPoint(ts, value));
            return this;
        }

        public SeriesScope point(Instant ts, double value, Status status) {
            target.points.add(new DataPoint(ts, value, status));
            return this;
        }

        public SeriesScope point(DataPoint point) {
            target.points.add(point);
            return this;
        }

        public SeriesScope points(Collection<DataPoint> points) {
            target.points.addAll(points);
            return this;
        }

        /** Selects another series, returning its scope. */
        public SeriesScope series(long id) {
            return parent.series(id);
        }

        public SeriesScope ignoreInvalidTimestamps(boolean ignoreInvalidTimestamps) {
            parent.ignoreInvalidTimestamps(ignoreInvalidTimestamps);
            return this;
        }

        public SeriesScope chunkSize(int chunkSize) {
            parent.chunkSize(chunkSize);
            return this;
        }

        public void send() {
            parent.send();
        }
    }

    private static final class MutableSeries {

        private final long id;
        private final List<DataPoint> points = new ArrayList<>();

        MutableSeries(long id) {
            this.id = id;
        }
    }
}
