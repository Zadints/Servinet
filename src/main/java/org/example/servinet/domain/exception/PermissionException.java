package org.example.servinet.domain.exception;

public class PermissionException extends RuntimeException {
    public PermissionException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Error de permiso: " + super.getMessage();
    }
}
