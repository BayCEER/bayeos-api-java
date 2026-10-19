package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Paginated result of {@code GET /nodes/search}, shaped as
 * {@code { total, hits }}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class NodeSearchResult {

    @JsonProperty("total")
    private int total;

    @JsonProperty("hits")
    private List<Node> hits;

    NodeSearchResult() {
    }

    public int getTotal() {
        return total;
    }

    public List<Node> getHits() {
        return hits == null ? Collections.emptyList() : hits;
    }
}
