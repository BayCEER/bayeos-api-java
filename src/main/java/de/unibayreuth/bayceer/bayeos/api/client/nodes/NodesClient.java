package de.unibayreuth.bayceer.bayeos.api.client.nodes;

import de.unibayreuth.bayceer.bayeos.api.client.http.HttpTransport;
import de.unibayreuth.bayceer.bayeos.api.client.http.Request;
import de.unibayreuth.bayceer.bayeos.api.client.model.Node;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fluent access to the {@code /nodes} endpoints: read the node tree, search by
 * Ant path and create/update/delete nodes.
 */
public final class NodesClient {

    private final HttpTransport http;

    public NodesClient(HttpTransport http) {
        this.http = http;
    }

    /** Fetches a single node by id ({@code GET /nodes/{id}}). */
    public Node get(long id) {
        return http.execute(Request.get("/nodes/" + id), Node.class);
    }

    /**
     * Lists nodes, optionally filtered by ids ({@code GET /nodes?ids=…}). With
     * no ids all readable nodes are returned.
     */
    public List<Node> list(long... ids) {
        Request request = Request.get("/nodes");
        if (ids.length > 0) {
            request.query("ids", joinIds(ids));
        }
        return http.executeList(request, Node.class);
    }

    /** Direct children of a node ({@code GET /nodes/{id}/children}). */
    public List<Node> children(long id) {
        return http.executeList(Request.get("/nodes/" + id + "/children"), Node.class);
    }

    /** Ancestors of a node ({@code GET /nodes/{id}/parents}). */
    public List<Node> parents(long id) {
        return http.executeList(Request.get("/nodes/" + id + "/parents"), Node.class);
    }

    /** Starts an Ant path search ({@code GET /nodes/search}). */
    public NodeSearchBuilder search() {
        return new NodeSearchBuilder(http);
    }

    /**
     * Creates a folder under an explicit parent id. A parent (or a
     * {@link NodeCreateBuilder#path(String) path}) must be provided before
     * {@link NodeCreateBuilder#execute()}.
     */
    public NodeCreateBuilder createFolder(String displayName) {
        return new NodeCreateBuilder(http, displayName, de.unibayreuth.bayceer.bayeos.api.client.model.NodeKind.FOLDER);
    }

    /** Creates a node of the given kind under an explicit parent id. */
    public NodeCreateBuilder create(String displayName, de.unibayreuth.bayceer.bayeos.api.client.model.NodeKind kind) {
        return new NodeCreateBuilder(http, displayName, kind);
    }

    /**
     * Creates a node from a slash-delimited path with {@code mkdir -p} semantics:
     * intermediate {@code Folder} nodes are created as needed. The path must start
     * with {@code /} (relative to the root) or {@code ~} (relative to the caller's
     * home node); the last segment is used as the display name.
     *
     * <p>The {@code ~} prefix requires credential-based sign-in — a node token has
     * no home node.</p>
     */
    public NodeCreateBuilder createPath(String path) {
        return createPath(path, de.unibayreuth.bayceer.bayeos.api.client.model.NodeKind.FOLDER);
    }

    /** Like {@link #createPath(String)} but with an explicit node kind. */
    public NodeCreateBuilder createPath(String path, de.unibayreuth.bayceer.bayeos.api.client.model.NodeKind kind) {
        return new NodeCreateBuilder(http, leafName(path), kind).path(path);
    }

    /** Updates arbitrary fields of a node ({@code PATCH /nodes/{id}}). */
    public void update(long id, Map<String, Object> fields) {
        http.execute(Request.patch("/nodes/" + id, fields));
    }

    /** Deletes a node and its descendants ({@code DELETE /nodes/{id}}). */
    public void delete(long id) {
        http.execute(Request.delete("/nodes/" + id));
    }

    public static String joinIds(long... ids) {
        return Arrays.stream(ids).mapToObj(Long::toString).collect(Collectors.joining(","));
    }

    static String leafName(String path) {
        String trimmed = path;
        while (trimmed.endsWith("/") && trimmed.length() > 1) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        int lastSlash = trimmed.lastIndexOf('/');
        String leaf = lastSlash >= 0 ? trimmed.substring(lastSlash + 1) : trimmed;
        return leaf.isEmpty() ? trimmed : leaf;
    }
}
