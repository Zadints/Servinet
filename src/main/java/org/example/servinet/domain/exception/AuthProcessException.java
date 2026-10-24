package org.example.servinet.domain.exception;

public class AuthProcessException extends RuntimeException {
    public AuthProcessException(String message) {
        super(message);
    }
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
