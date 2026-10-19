package de.unibayreuth.bayceer.bayeos.api.client.nodes;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.Node;
import de.unibayreuth.bayceer.bayeos.api.client.model.NodeKind;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder for {@code POST /nodes}.
 *
 * <p>Either {@link #parentId(long)} or {@link #path(String)} must be set.
 * A {@code path} starting with {@code /} is resolved against the root, one
 * starting with {@code ~} against the caller's home node; intermediate folders
 * are created automatically ({@code mkdir -p}). The two are mutually exclusive.</p>
 */
public final class NodeCreateBuilder {

    private final HttpTransport http;
    private final String displayName;
    private final NodeKind kind;
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    private Long parentId;
    private String path;

    NodeCreateBuilder(HttpTransport http, String displayName, NodeKind kind) {
        this.http = http;
        this.displayName = displayName;
        this.kind = kind;
    }

    /** Sets the parent node id. Mutually exclusive with {@link #path(String)}. */
    public NodeCreateBuilder parentId(long parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * Sets a slash-delimited path (starting with {@code /} or {@code ~}).
     * Intermediate folders are created as needed. Mutually exclusive with
     * {@link #parentId(long)}.
     */
    public NodeCreateBuilder path(String path) {
        this.path = path;
        return this;
    }

    /** Adds a single metadata entry. */
    public NodeCreateBuilder metadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /** Sets the {@code unit} metadata property. */
    public NodeCreateBuilder unit(String unit) {
        return metadata("unit", unit);
    }

    /** Sets the {@code resolution} metadata property (in whole seconds). */
    public NodeCreateBuilder resolution(Duration resolution) {
        return metadata("resolution", resolution.getSeconds());
    }

    /** Sets the {@code lat} metadata property. */
    public NodeCreateBuilder lat(double latitude) {
        return metadata("lat", latitude);
    }

    /** Sets the {@code lon} metadata property. */
    public NodeCreateBuilder lon(double longitude) {
        return metadata("lon", longitude);
    }

    /** Executes the request and returns the created node. */
    public Node execute() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("display_name", displayName);
        body.put("kind", kind.value());
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
        return http.execute(Request.post("/nodes", body), Node.class);
    }
}
