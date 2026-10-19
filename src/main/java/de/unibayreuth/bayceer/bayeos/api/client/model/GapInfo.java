package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * A detected gap in a timeseries, returned by
 * {@code GET /timeseries/{id}/gaps}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GapInfo {

    @JsonProperty("start")
    private Instant start;

    @JsonProperty("end")
    private Instant end;

    @JsonProperty("duration_minutes")
    private int durationMinutes;

    GapInfo() {
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    @Override
    public String toString() {
        return "GapInfo{start=" + start + ", end=" + end + ", durationMinutes=" + durationMinutes + '}';
    }
}
