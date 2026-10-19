package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.DataPoint;
import de.unibayreuth.bayceer.bayeos.api.client.model.Status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fluent builder for writing points to a single series
 * ({@code POST /timeseries/{id}/data}). The API upserts on {@code (id, ts)}.
 */
public final class DataWriteBuilder {

    private final HttpTransport http;
    private final long id;
    private final List<DataPoint> points = new ArrayList<>();
    private boolean ignoreInvalidTimestamps;

    DataWriteBuilder(HttpTransport http, long id) {
        this.http = http;
        this.id = id;
    }

    /** Adds a point without an explicit status. */
    public DataWriteBuilder point(Instant ts, double value) {
        points.add(new DataPoint(ts, value));
        return this;
    }

    /** Adds a point with an explicit status. */
    public DataWriteBuilder point(Instant ts, double value, Status status) {
        points.add(new DataPoint(ts, value, status));
        return this;
    }

    /** Adds a pre-built point. */
    public DataWriteBuilder point(DataPoint point) {
        points.add(point);
        return this;
    }

    /** Adds several points. */
    public DataWriteBuilder points(Collection<DataPoint> points) {
        this.points.addAll(points);
        return this;
    }

    /** Silently ignores timestamps the server considers invalid. */
    public DataWriteBuilder ignoreInvalidTimestamps(boolean ignoreInvalidTimestamps) {
        this.ignoreInvalidTimestamps = ignoreInvalidTimestamps;
        return this;
    }

    /** Sends the accumulated points. */
    public void send() {
        Request request = Request.post("/timeseries/" + id + "/data", points);
        if (ignoreInvalidTimestamps) {
            request.query("ignore_invalid_timestamps", true);
        }
        http.execute(request);
    }
}
