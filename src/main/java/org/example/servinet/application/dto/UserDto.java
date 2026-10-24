package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.Role;

import java.nio.file.Path;

//Si vas a editar entidad también edita SQL SERVER :DDD

public class UserDto {
    private String name;
    private String email;
    private Role rol;
    private byte[] perfilImg;


    public UserDto(String name, Role rol, String email, byte[] image) {
        this.name = name;
        this.rol = rol;
        this.email = email;
        this.perfilImg = image;
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

    public byte[] getPerfilImg() {
        return perfilImg;
    }
}
