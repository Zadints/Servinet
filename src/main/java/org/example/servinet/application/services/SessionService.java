package org.example.servinet.application.services;

import org.example.servinet.application.dto.UserDto;
import org.example.servinet.domain.entities.User;
import org.example.servinet.domain.enums.Role;
import org.example.servinet.models.UserModel;
import org.example.servinet.utils.ImageConverter;
import org.example.servinet.utils.PasswordHash;
import org.example.servinet.utils.UuidGenerate;

import java.io.IOException;
import java.time.LocalDateTime;

public class SessionService {
    private static User actualUser = null;
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$";

    public static boolean loginUser(UserDto newUser){

        User userExist = UserModel.getUserDatabase(newUser.getName());

        if (PasswordHash.comparePassword(userExist.getPasswordHash(), newUser.getPasswordBrute())){
            return true;
        }

        return false;
    }
    public static boolean registerUser(UserDto newUser) throws IOException {

        if (newUser.getName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")){
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras y espacios."
            );
        }

        if (!newUser.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")){
            throw new IllegalArgumentException(
                    "El correo ingresado no es válido"
            );
        }

        String tmpPass = newUser.getPasswordBrute();

        if (tmpPass.isBlank()){
            throw new IllegalArgumentException(
                    "La contraseña ingresada está vacia."
            );
        }

        if (!tmpPass.matches(PASSWORD_REGEX)){
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres, "
                    + "una mayúscula, una minúscula, un número y un carácter especial."
            );
        }

        actualUser = new User(
                UuidGenerate.getNewUuid(),
                newUser.getEmail(),
                newUser.getRol(),
                LocalDateTime.now(),
                PasswordHash.hashPassword(tmpPass),
                newUser.getName(),
                ImageConverter.toBase64(newUser.getPerfilImg())
        );

        UserModel.setUserDatabase(actualUser);
        return true;
    }

    public static User getActualSessionUser() {
        if (actualUser == null)
            throw new NullPointerException("No puedes solicitar datos del usuario si no existen");

        return actualUser;
    }

    public static void closeSessionUser() {
        actualUser = null;
    }

    public static String getUserUuid() {
        return actualUser.getUuid();
    }

    public static String getUserName() {
        return actualUser.getName();
    }

    public static String getUserEmail() {
        return actualUser.getName();
    }

    public static Role getUserRol() {
        return actualUser.getRol();
    }

    public static LocalDateTime getUserCreateAt() {
        return actualUser.getCreateAt();
    }

    public static String getUserPerfilImg() {
        return actualUser.getPerfilImg();
    }

}
