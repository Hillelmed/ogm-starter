package io.github.hillelmed.ogm.starter.exception;

/**
 * The type Ogm runtime exception.
 */
public class OgmRuntimeException extends RuntimeException {

    /**
     * Instantiates a new Ogm runtime exception.
     *
     * @param cause the cause
     */
    public OgmRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Ogm runtime exception.
     *
     * @param message the message
     */
    public OgmRuntimeException(String message) {
        super(message);
    }
}
