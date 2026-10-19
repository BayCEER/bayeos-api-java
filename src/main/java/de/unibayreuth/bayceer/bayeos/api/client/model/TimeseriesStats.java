package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Statistical metadata of a timeseries, returned by
 * {@code GET /timeseries/{id}/stats}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TimeseriesStats {

    @JsonProperty("count")
    private long count;

    @JsonProperty("first_ts")
    private Instant firstTs;

    @JsonProperty("last_ts")
    private Instant lastTs;

    @JsonProperty("min")
    private Double min;

    @JsonProperty("max")
    private Double max;

    @JsonProperty("mean")
    private Double mean;

    @JsonProperty("stddev")
    private Double stddev;

    @JsonProperty("completeness")
    private CompletenessStats completeness;

    TimeseriesStats() {
    }

    public long getCount() {
        return count;
    }

    public Instant getFirstTs() {
        return firstTs;
    }

    public Instant getLastTs() {
        return lastTs;
    }

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }

    public Double getMean() {
        return mean;
    }

    public Double getStddev() {
        return stddev;
    }

    public CompletenessStats getCompleteness() {
        return completeness;
    }

    /** Completeness statistics over a resolution-based expected count. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class CompletenessStats {

        @JsonProperty("expected_count")
        private long expectedCount;

        @JsonProperty("actual_count")
        private long actualCount;

        @JsonProperty("missing_count")
        private long missingCount;

        @JsonProperty("completeness_pct")
        private double completenessPct;

        CompletenessStats() {
        }

        public long getExpectedCount() {
            return expectedCount;
        }

        public long getActualCount() {
            return actualCount;
        }

        public long getMissingCount() {
            return missingCount;
        }

        public double getCompletenessPct() {
            return completenessPct;
        }
    }
}
