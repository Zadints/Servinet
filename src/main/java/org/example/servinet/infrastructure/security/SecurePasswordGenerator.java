package org.example.servinet.infrastructure.security;
import org.example.servinet.application.port.out.PasswordGenerator;

import java.security.SecureRandom;

public class SecurePasswordGenerator implements PasswordGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "0123456789!@#$%&*_-+=";

    private final SecureRandom random = new SecureRandom();

    public String generate(int length) {
        if (length < 8) {
            throw new IllegalArgumentException(
                    "La longitud mínima es de 8 caracteres");
        }

        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            password.append(
                    CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))
            );
        }

        return password.toString();
    }
}
