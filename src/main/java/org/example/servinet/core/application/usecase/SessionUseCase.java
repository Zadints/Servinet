package org.example.servinet.core.application.usecase;

import javafx.scene.image.Image;
import org.example.servinet.core.application.dto.UserDto;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.LogType;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.FileExistException;
import org.example.servinet.core.domain.exception.InvalidCredentialsException;
import org.example.servinet.core.domain.exception.RoleNoPermission;
import org.example.servinet.core.domain.utils.GetHadware;
import org.example.servinet.infrastructure.database.models.UserModel;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.core.domain.utils.PasswordHash;
import org.example.servinet.core.application.service.UuidGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionUseCase {

    private static User actualUser = null;
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$";
    private static List<User> usersList = new ArrayList<>();

    public static List<User> getUsersList() {
        return usersList;
    }

    public static boolean automaticUserLogin(String hardwareId){
        if (hardwareId == null || hardwareId.isBlank()) return false;

        actualUser = UserModel.getUserDatabaseHadwareId(hardwareId);
        if (actualUser == null) return false;

        System.out.println("si tiene cuenta abrierta");
        usersList.add(actualUser);
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
            actualUser = userExist;
            usersList.add(userExist);
            LogsUseCase.addLog(LogType.LOGIN, "Inicio de sesión");
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

        Image image;
        byte[] imageToSaveDb;
        try {
            image = ImageConverter.toImage(newUser.getPerfilImg());
            imageToSaveDb = ImageConverter.toBytes(newUser.getPerfilImg());
        }catch (IOException e){
            throw new FileExistException(newUser.getPerfilImg());
        }

        String uuid = new GenerateIdUseCase(new UuidGenerator()).execute();

        actualUser = new User(
                uuid,
                newUser.getEmail(),
                newUser.getRol(),
                LocalDateTime.now(),
                PasswordHash.hashPassword(tmpPass),
                newUser.getName(),
                image
        );
        usersList.add(actualUser);
        UserModel.setUserDatabase(actualUser, imageToSaveDb);
        UserModel.setUserDatabaseHadwareId(GetHadware.id(), uuid);
        return true;
    }

    public static User getActualSessionUser() {
        if (actualUser == null)
            throw new NullPointerException("No puedes solicitar datos del usuario si no existen");

        return actualUser;
    }

    public static void closeSessionUser() {
        Thread thread = new Thread(() -> {
            UserModel.deleteUserSession(GetHadware.id());
            actualUser = null;
        });

        thread.setDaemon(true);
        thread.start();

    }

    public static String getUserUuid() {
        return actualUser.getUuid();
    }

    public static String getUserName() {
        return actualUser.getName();
    }

    public static String getUserEmail() {
        return actualUser.getEmail();
    }

    public static Role getUserRol() {
        return actualUser.getRol();
    }
    public static String getStringUserRol(){
        return actualUser.getRolName();
    }

    public static LocalDateTime getUserCreateAt() {
        return actualUser.getCreateAt();
    }

    public static Image getUserPerfilImg() {
        return actualUser.getPerfilImg();
    }

    public static boolean isEqualsPasswordUser(String password){

        if (PasswordHash.comparePassword(actualUser.getPasswordHash(), password)){
            return true;
        }
        return false;
    }

    public static void loadAllUsers() {
        usersList = UserModel.getAllUsers();
    }

    private static void validateNameAndEmail(String name, String email) {
        if (name == null || !name.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new IllegalArgumentException("El nombre solo puede contener letras y espacios.");
        }
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new IllegalArgumentException("El correo ingresado no es válido");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || !password.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula, "
                            + "una minúscula, un número y un carácter especial.");
        }
    }

    // Crea un usuario SIN cambiar la sesión actual (registerUser sí la cambia)
    public static void createUser(UserDto newUser) {
        if (!PermissionValidation.hasPermission(Permission.AD_CREATE_USER)) {
            throw new RoleNoPermission("No tienes permiso para crear usuarios");
        }
        validateNameAndEmail(newUser.getName(), newUser.getEmail());
        if (newUser.getRol() == null) {
            throw new IllegalArgumentException("Debes seleccionar un rol");
        }
        validatePassword(newUser.getPasswordBrute());
        if (newUser.getPerfilImg() == null) {
            throw new IllegalArgumentException("Debes seleccionar una imagen de perfil");
        }
        if (UserModel.isNameOrEmailTaken(newUser.getName(), newUser.getEmail(), null)) {
            throw new InvalidCredentialsException("Ya existe un usuario con ese nombre o correo");
        }

        Image image;
        byte[] imageBytes;
        try {
            image = ImageConverter.toImage(newUser.getPerfilImg());
            imageBytes = ImageConverter.toBytes(newUser.getPerfilImg());
        } catch (IOException e) {
            throw new FileExistException(newUser.getPerfilImg());
        }

        String uuid = new GenerateIdUseCase(new UuidGenerator()).execute();
        User user = new User(
                uuid,
                newUser.getEmail(),
                newUser.getRol(),
                LocalDateTime.now(),
                PasswordHash.hashPassword(newUser.getPasswordBrute()),
                newUser.getName(),
                image
        );
        UserModel.setUserDatabase(user, imageBytes);
        LogsUseCase.addLog(LogType.USER_CREATE, "Creó al usuario " + newUser.getName());
        loadAllUsers();
    }

    public static void editUser(User target, String name, String email, Role rol,
                                String newPassword, Path newImage) {
        if (!PermissionValidation.hasPermission(Permission.AD_EDIT_USER)) {
            throw new RoleNoPermission("No tienes permiso para editar usuarios");
        }
        validateNameAndEmail(name, email);
        if (rol == null) {
            throw new IllegalArgumentException("Debes seleccionar un rol");
        }

        boolean isMe = actualUser != null && actualUser.getUuid().equalsIgnoreCase(target.getUuid());
        if (isMe && !target.getRol().getUuid().equalsIgnoreCase(rol.getUuid())) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol.");
        }

        String newHash = null;
        if (newPassword != null && !newPassword.isBlank()) {
            validatePassword(newPassword);
            newHash = PasswordHash.hashPassword(newPassword);
        }

        byte[] imageBytes = null;
        if (newImage != null) {
            try {
                imageBytes = ImageConverter.toBytes(newImage);
            } catch (IOException e) {
                throw new FileExistException(newImage);
            }
        }

        if (UserModel.isNameOrEmailTaken(name, email, target.getUuid())) {
            throw new InvalidCredentialsException("Ya existe otro usuario con ese nombre o correo");
        }

        UserModel.updateUser(target.getUuid(), name, email, rol.getUuid(), newHash, imageBytes);
        LogsUseCase.addLog(LogType.USER_EDIT, "Editó al usuario " + target.getName());

        if (isMe) {
            actualUser = UserModel.getUserDatabaseUuid(target.getUuid());
        }
        loadAllUsers();
    }

    public static void deleteUser(User target) {
        if (!PermissionValidation.hasPermission(Permission.AD_DELETE_USER)) {
            throw new RoleNoPermission("No tienes permiso para eliminar usuarios");
        }
        if (actualUser != null && actualUser.getUuid().equalsIgnoreCase(target.getUuid())) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }
        if (target.getRol().hasPermission(Permission.BYPASS)) {
            throw new IllegalArgumentException("No se puede eliminar a un usuario con rol de Dueño.");
        }
        UserModel.deleteUser(target.getUuid());
        LogsUseCase.addLog(LogType.USER_DELETE, "Eliminó al usuario " + target.getName());
        loadAllUsers();
    }

    public static void changeOwnPassword(String current, String newPassword, String confirm) {
        if (current == null || current.isBlank() || newPassword == null || confirm == null) {
            throw new IllegalArgumentException("Debes rellenar todos los campos.");
        }
        if (!isEqualsPasswordUser(current)) {
            throw new InvalidCredentialsException("La contraseña actual es incorrecta.");
        }
        if (!newPassword.equals(confirm)) {
            throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden.");
        }
        if (newPassword.equals(current)) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta a la actual.");
        }
        validatePassword(newPassword);

        UserModel.updatePassword(actualUser.getUuid(), PasswordHash.hashPassword(newPassword));
        actualUser = UserModel.getUserDatabaseUuid(actualUser.getUuid());
        LogsUseCase.addLog(LogType.PASSWORD_CHANGE, "Cambió su contraseña");
    }
}
