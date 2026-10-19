package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Validation status of a timeseries data point.
 */
public enum Status {

    /** The value was removed. */
    REMOVED(-1),
    /** Newly imported or automatically inserted data. */
    NOT_SET(0),
    /** Validated as valid. */
    VALID(1),
    /** Valid with remarks. */
    VALID_WITH_REMARKS(2),
    /** Validated as noisy. */
    NOISY(3),
    /** Uncertain data. */
    UNKNOWN(4),
    /** Measured value was overrange. */
    OVERRANGE(5),
    /** Measured value was outside the limits. */
    OUTSIDE_LIMITS(6),
    /** Validated as invalid. */
    INVALID(7);

    private final int code;

    Status(int code) {
        this.code = code;
    }

    @JsonValue
    public int code() {
        return code;
    }

    @JsonCreator
    public static Status fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (Status s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
