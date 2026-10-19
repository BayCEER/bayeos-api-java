package de.unibayreuth.bayceer.bayeos.api.client.timeseries;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/** Shared timestamp formatting for query parameters. */
final class Times {

    private Times() {
    }

    /** Formats an {@link Instant} as an RFC3339 string (UTC). */
    static String iso(Instant instant) {
        return instant == null ? null : DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
