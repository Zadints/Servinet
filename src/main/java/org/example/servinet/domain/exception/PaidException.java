package org.example.servinet.domain.exception;

public class PaidException extends RuntimeException {
    public PaidException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
