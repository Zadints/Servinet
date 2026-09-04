package org.example.servinet.utils;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHash {

    private static Argon2 argon2 = Argon2Factory.create();

    public static String hashPassword(String password){


        return argon2.hash(3, 65536, 1, password);
    }

    public static boolean comparePassword(String pasword, String newPassword){
        return argon2.verify(pasword, newPassword);
    }
}
