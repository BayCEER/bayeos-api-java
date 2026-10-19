package de.unibayreuth.bayceer.bayeos.api.client.model;

/**
 * Rollup granularity for timeseries queries.
 */
public enum SampleInterval {

    HOURLY("1h"),
    DAILY("1d"),
    MONTHLY("1M"),
    YEARLY("1y");

    private final String code;

    SampleInterval(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
