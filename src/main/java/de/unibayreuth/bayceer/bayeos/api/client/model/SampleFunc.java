package de.unibayreuth.bayceer.bayeos.api.client.model;

/**
 * Aggregate function applied within a rollup interval.
 */
public enum SampleFunc {

    AVG("avg"),
    COUNT("count"),
    SUM("sum"),
    LAST("last"),
    MIN("min"),
    MAX("max"),
    FIRST("first");

    private final String code;

    SampleFunc(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
