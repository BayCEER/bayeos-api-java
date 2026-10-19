package de.unibayreuth.bayceer.bayeos.api.client.exception;

import de.unibayreuth.bayceer.bayeos.api.client.model.ErrorResponse;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Raised when the API answers with a non-2xx status.
 *
 * <p>Carries the parsed {@link ErrorResponse} body (when present) together with
 * the numeric HTTP status code.</p>
 */
public class ApiException extends BayeosException {

    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final String error;
    private final String serverMessage;
    private final String traceId;
    private final Instant timestamp;
    private final Map<String, Object> details;
    private final String rawBody;

    public ApiException(int statusCode, ErrorResponse body, String rawBody) {
        super(buildMessage(statusCode, body, rawBody));
        this.statusCode = statusCode;
        this.rawBody = rawBody;
        this.error = body != null ? body.getError() : null;
        this.serverMessage = body != null ? body.getMessage() : null;
        this.traceId = body != null ? body.getTraceId() : null;
        this.timestamp = body != null ? body.getTimestamp() : null;
        this.details = body != null && body.getDetails() != null
                ? body.getDetails()
                : Collections.emptyMap();
    }

    /** HTTP status code returned by the server. */
    public int getStatusCode() {
        return statusCode;
    }

    /** Machine-readable error code, e.g. {@code InvalidRequest}. */
    public String getError() {
        return error;
    }

    /** Human-readable message returned by the server. */
    public String getServerMessage() {
        return serverMessage;
    }

    /** Unique identifier for error tracing, or {@code null}. */
    public String getTraceId() {
        return traceId;
    }

    /** Server-side timestamp of the error, or {@code null}. */
    public Instant getTimestamp() {
        return timestamp;
    }

    /** Additional error context, never {@code null}. */
    public Map<String, Object> getDetails() {
        return details;
    }

    /** Raw response body (may be empty), useful when it is not valid JSON. */
    public String getRawBody() {
        return rawBody;
    }

    private static String buildMessage(int statusCode, ErrorResponse body, String rawBody) {
        if (body != null && body.getMessage() != null) {
            String code = body.getError() != null ? " (" + body.getError() + ")" : "";
            return "HTTP " + statusCode + ": " + body.getMessage() + code;
        }
        if (rawBody != null && !rawBody.isEmpty()) {
            return "HTTP " + statusCode + ": " + rawBody;
        }
        return "HTTP " + statusCode;
    }
}
