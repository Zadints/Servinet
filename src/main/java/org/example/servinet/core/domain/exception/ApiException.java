package org.example.servinet.core.domain.exception;

public class ApiException extends RuntimeException {

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
    public ApiException(String message) {
        super(message);
    }
}
