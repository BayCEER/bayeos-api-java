package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * A single timestamped measurement, used as the write element of
 * {@code POST /timeseries/{id}/data}.
 */
public final class DataPoint {

    private final Instant ts;
    private final double value;
    private final Status status;

    public DataPoint(Instant ts, double value) {
        this(ts, value, null);
    }

    public DataPoint(Instant ts, double value, Status status) {
        this.ts = Objects.requireNonNull(ts, "ts");
        this.value = value;
        this.status = status;
    }

    @JsonProperty("ts")
    public Instant getTs() {
        return ts;
    }

    @JsonProperty("value")
    public double getValue() {
        return value;
    }

    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "DataPoint{ts=" + ts + ", value=" + value
                + (status != null ? ", status=" + status : "") + '}';
    }
}
