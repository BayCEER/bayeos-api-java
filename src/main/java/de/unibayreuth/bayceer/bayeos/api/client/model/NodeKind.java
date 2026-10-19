package de.unibayreuth.bayceer.bayeos.api.client.model;

/**
 * Well-known node kinds used by the BayEOS API.
 */
public enum NodeKind {

    FOLDER("Folder"),
    TIMESERIES("Timeseries");

    private final String value;

    NodeKind(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
