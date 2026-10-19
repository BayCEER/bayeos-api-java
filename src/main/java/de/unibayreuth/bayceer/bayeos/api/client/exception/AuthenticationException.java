package de.unibayreuth.bayceer.bayeos.api.client.exception;

import de.unibayreuth.bayceer.bayeos.api.client.model.ErrorResponse;

/**
 * Raised when the API rejects a request with HTTP 401 (missing, expired or
 * invalid credentials).
 */
public class AuthenticationException extends ApiException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(int statusCode, ErrorResponse body, String rawBody) {
        super(statusCode, body, rawBody);
    }
}
