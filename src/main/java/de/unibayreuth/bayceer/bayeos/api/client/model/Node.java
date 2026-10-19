package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * A node in the BayEOS node tree, which is also the projection used for
 * timeseries metadata ({@code kind == "Timeseries"}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Node {

    @JsonProperty("id")
    private long id;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("parent_id")
    private int parentId;

    @JsonProperty("permission")
    private int permission;

    @JsonProperty("node_path")
    private String nodePath;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    Node() {
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKind() {
        return kind;
    }

    public int getParentId() {
        return parentId;
    }

    public int getPermission() {
        return permission;
    }

    public String getNodePath() {
        return nodePath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /** Convenience accessor for a well-known metadata key (may be {@code null}). */
    public Object getMetadata(String key) {
        return metadata == null ? null : metadata.get(key);
    }

    @Override
    public String toString() {
        return "Node{id=" + id + ", displayName='" + displayName + "', kind='" + kind
                + "', nodePath='" + nodePath + "'}";
    }
}
