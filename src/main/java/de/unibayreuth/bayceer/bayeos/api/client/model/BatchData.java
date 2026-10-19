package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Column-oriented timeseries data. Returned by both
 * {@code GET /timeseries/data} (several series) and
 * {@code GET /timeseries/{id}/data} (a single series).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class BatchData {

    @JsonProperty("ts_type")
    private String tsType;

    @JsonProperty("sample_interval")
    private String sampleInterval;

    @JsonProperty("sample_func")
    private String sampleFunc;

    @JsonProperty("series")
    private List<SeriesData> series;

    BatchData() {
    }

    public String getTsType() {
        return tsType;
    }

    public String getSampleInterval() {
        return sampleInterval;
    }

    public String getSampleFunc() {
        return sampleFunc;
    }

    public List<SeriesData> getSeries() {
        return series == null ? Collections.emptyList() : series;
    }

    /**
     * Returns the first series, or {@code null} when the response carries no
     * series. Convenient for single-series reads.
     */
    public SeriesData first() {
        List<SeriesData> s = getSeries();
        return s.isEmpty() ? null : s.get(0);
    }
}
