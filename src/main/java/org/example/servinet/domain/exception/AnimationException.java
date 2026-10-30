package org.example.servinet.domain.exception;

public class AnimationException extends RuntimeException {
    public AnimationException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Animated exception ->" + super.getMessage();
    }
}
