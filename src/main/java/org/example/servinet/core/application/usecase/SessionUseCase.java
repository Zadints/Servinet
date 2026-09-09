package org.example.servinet.core.application.usecase;

import org.example.servinet.core.application.dto.UserDto;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.Role;
import org.example.servinet.core.domain.exception.FileExistException;
import org.example.servinet.core.domain.exception.InvalidCredentialsException;
import org.example.servinet.core.domain.utils.GetHadware;
import org.example.servinet.infrastructure.database.models.UserModel;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.core.domain.utils.PasswordHash;
import org.example.servinet.core.domain.utils.UuidGenerate;

import java.io.IOException;
import java.time.LocalDateTime;

public class SessionUseCase {

    private static User actualUser = null;
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$";

    public static boolean automaticUserLogin(String hardwareId){
        if (hardwareId == null || hardwareId.isBlank()) return false;

        actualUser = UserModel.getUserDatabaseHadwareId(hardwareId);
        if (actualUser == null) return false;

        return true;
    }

    public static boolean loginUser(String user, String password){


        if (user == null || user.isBlank()) {
            throw new InvalidCredentialsException("Debes ingresar un nombre de usuario");
        }

        if (password == null || password.isBlank()) {
            throw new InvalidCredentialsException("Debes ingresar una contraseña");
        }

        User userExist = UserModel.getUserDatabase(user);

        if (userExist == null){
            throw new InvalidCredentialsException(
                    "Usuario o contraseña incorrectos"
            );
        }

        if (PasswordHash.comparePassword(userExist.getPasswordHash(), password)){
            UserModel.setUserDatabaseHadwareId(GetHadware.id(), userExist.getUuid());
            return true;
        }

        return false;
    }
    public static boolean registerUser(UserDto newUser) {

        if (!newUser.getName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")){
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

        User userExist = UserModel.getUserDatabase(newUser.getName());

        if (userExist != null){
            throw new InvalidCredentialsException(
                    "no puedes registrar el mismo usuario dos veces"
            );
        }

        String image = "";
        try {
            image = ImageConverter.toBase64(newUser.getPerfilImg());
        }catch (IOException e){
            throw new FileExistException(newUser.getPerfilImg());
        }

        String uuid = UuidGenerate.getNewUuid();

        actualUser = new User(
                uuid,
                newUser.getEmail(),
                newUser.getRol(),
                LocalDateTime.now(),
                PasswordHash.hashPassword(tmpPass),
                newUser.getName(),
                image
        );

        UserModel.setUserDatabase(actualUser);
        UserModel.setUserDatabaseHadwareId(GetHadware.id(), uuid);
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
