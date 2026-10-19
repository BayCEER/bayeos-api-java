package de.unibayreuth.bayceer.bayeos.api.client.exception;

/**
 * Raised when the underlying HTTP request cannot be executed or its response
 * cannot be parsed.
 */
public class TransportException extends BayeosException {

    private static final long serialVersionUID = 1L;

    public TransportException(String message) {
        super(message);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
