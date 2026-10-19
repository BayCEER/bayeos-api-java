package de.unibayreuth.bayceer.bayeos.api.client.model;

/**
 * Timestamp encoding of timeseries responses.
 */
public enum TsType {

    /** RFC3339 strings (the API default). */
    ISO("iso"),
    /** Epoch microseconds. */
    EPOCH("epoch");

    private final String code;

    TsType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
