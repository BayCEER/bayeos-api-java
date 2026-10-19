package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.Node;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder for {@code POST /timeseries}.
 *
 * <p>Either {@link #parentId(long)} or {@link #path(String)} must be set. A
 * {@code path} creates intermediate folders as needed; the last segment is used
 * as the series name when it is not supplied via the constructor.</p>
 */
public final class TimeseriesCreateBuilder {

    private final HttpTransport http;
    private final String displayName;
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    private Long parentId;
    private String path;

    TimeseriesCreateBuilder(HttpTransport http, String displayName) {
        this.http = http;
        this.displayName = displayName;
    }

    /** Sets the parent node id. Mutually exclusive with {@link #path(String)}. */
    public TimeseriesCreateBuilder parentId(long parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * Sets a slash-delimited path (starting with {@code /} or {@code ~}).
     * Intermediate folders are created as needed. Mutually exclusive with
     * {@link #parentId(long)}.
     */
    public TimeseriesCreateBuilder path(String path) {
        this.path = path;
        return this;
    }

    /** Adds a single metadata entry. */
    public TimeseriesCreateBuilder metadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /** Sets the {@code unit} metadata property. */
    public TimeseriesCreateBuilder unit(String unit) {
        return metadata("unit", unit);
    }

    /** Sets the {@code resolution} metadata property (in whole seconds). */
    public TimeseriesCreateBuilder resolution(Duration resolution) {
        return metadata("resolution", resolution.getSeconds());
    }

    /** Sets the {@code lat} metadata property. */
    public TimeseriesCreateBuilder lat(double latitude) {
        return metadata("lat", latitude);
    }

    /** Sets the {@code lon} metadata property. */
    public TimeseriesCreateBuilder lon(double longitude) {
        return metadata("lon", longitude);
    }

    /** Executes the request and returns the created timeseries node. */
    public Node execute() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("display_name", displayName);
        if (path != null && parentId != null) {
            throw new IllegalStateException("Set either parentId or path, not both");
        }
        if (path != null) {
            body.put("node_path", path);
        } else if (parentId != null) {
            body.put("parent_id", parentId);
        } else {
            throw new IllegalStateException("Either parentId or path is required");
        }
        if (!metadata.isEmpty()) {
            body.put("metadata", metadata);
        }
        return http.execute(Request.post("/timeseries", body), Node.class);
    }
}
