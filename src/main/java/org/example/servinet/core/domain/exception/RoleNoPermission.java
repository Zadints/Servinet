package org.example.servinet.core.domain.exception;

public class RoleNoPermission extends RuntimeException {
    public RoleNoPermission(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Error de permiso: " + super.getMessage();
    }
}
