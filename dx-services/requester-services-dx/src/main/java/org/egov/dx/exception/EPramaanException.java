package org.egov.dx.exception;

public class EPramaanException extends RuntimeException {
    public EPramaanException(String message) {
        super(message);
    }

    public EPramaanException(String message, Throwable cause) {
        super(message, cause);
    }
}
