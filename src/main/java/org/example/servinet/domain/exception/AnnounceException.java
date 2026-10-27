package org.example.servinet.domain.exception;

public class AnnounceException extends RuntimeException {
    public AnnounceException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Error de permiso: " + super.getMessage();
    }
}
