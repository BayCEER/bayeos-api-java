package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Column-oriented representation of one series within a read or write payload:
 * the id appears once, timestamps and values are parallel arrays and the
 * optional status array is parallel to the values.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SeriesData {

    @JsonProperty("id")
    private long id;

    @JsonProperty("ts")
    private List<Instant> ts;

    @JsonProperty("values")
    private List<Double> values;

    @JsonProperty("status")
    private List<Status> status;

    SeriesData() {
    }

    public long getId() {
        return id;
    }

    public List<Instant> getTs() {
        return ts == null ? Collections.emptyList() : ts;
    }

    public List<Double> getValues() {
        return values == null ? Collections.emptyList() : values;
    }

    public List<Status> getStatus() {
        return status == null ? Collections.emptyList() : status;
    }

    @Override
    public String toString() {
        return "SeriesData{id=" + id + ", points=" + getTs().size() + '}';
    }
}
