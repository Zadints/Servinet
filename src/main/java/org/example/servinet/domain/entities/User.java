package org.example.servinet.domain.entities;

import org.example.servinet.domain.enums.Role;
import org.example.servinet.domain.repository.Identifiable;
import org.example.servinet.exception.PasswordAlreadyHashedException;

import java.time.LocalDateTime;

//Si vas a editar entidad también edita SQL SERVER :DDD

public class User implements Identifiable {
    private final String uuid;
    private String name;
    private final String email;
    private final Role rol;
    private final String passwordHash;
    private final LocalDateTime createAt;
    private String perfilImg;

    public User(String uuid, String email, Role rol, LocalDateTime createAt, String passwordHash, String name, String perfilImg) {
        this.uuid = uuid;
        this.email = email;
        this.rol = rol;
        this.createAt = createAt;
        this.passwordHash = passwordHash;
        this.name = name;
        this.perfilImg = perfilImg;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setPerfilImg(String perfilImg) {
        this.perfilImg = perfilImg;
    }

    public String getUuid() {
        return uuid;
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

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public String getPerfilImg() {
        return perfilImg;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
