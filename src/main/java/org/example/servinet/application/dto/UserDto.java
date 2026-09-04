package org.example.servinet.application.dto;

import org.example.servinet.domain.enums.Role;

import java.nio.file.Path;

//Si vas a editar entidad también edita SQL SERVER :DDD

public class UserDto {
    private String name;
    private String email;
    private Role rol;
    private String passwordBrute;
    private Path perfilImg;


    public UserDto(String name, String passwordBrute, Role rol, String email, Path perfilImg) {
        this.name = name;
        this.passwordBrute = passwordBrute;
        this.rol = rol;
        this.email = email;
        this.perfilImg = perfilImg;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRol() {
        return rol;
    }

    public String getPasswordBrute() {
        return passwordBrute;
    }

    public Path getPerfilImg() {
        return perfilImg;
    }
}
