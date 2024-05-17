package io.github.hillelmed.ogm.starter.exception;

public class OgmRuntimeException extends RuntimeException {

    public OgmRuntimeException(Throwable cause) {
        super(cause);
    }

    public OgmRuntimeException(String message) {
        super(message);
    }
}
