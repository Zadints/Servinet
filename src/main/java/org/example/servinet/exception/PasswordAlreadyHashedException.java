package org.example.servinet.exception;

public class PasswordAlreadyHashedException extends RuntimeException {
    public PasswordAlreadyHashedException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Error de contraseña: " + super.getMessage();
    }
}
