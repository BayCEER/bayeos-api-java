package de.unibayreuth.bayceer.bayeos.api.client.exception;

/**
 * Base type for all errors raised by the BayEOS client.
 */
public class BayeosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BayeosException(String message) {
        super(message);
    }

    public BayeosException(String message, Throwable cause) {
        super(message, cause);
    }
}
