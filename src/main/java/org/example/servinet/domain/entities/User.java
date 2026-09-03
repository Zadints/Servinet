package org.example.servinet.domain.entities;

import org.example.servinet.domain.enums.Role;

import java.time.LocalDateTime;

//Si vas a editar entidad también edita SQL SERVER

public class User {
    private String id;
    private String name;
    private Role rol;
    private String passwordHash;
    private LocalDateTime createAt;
    private String perfilImg;


    public User(String id, String iname, Role rol, String passwordHash, LocalDateTime createAt, String perfilImg) {
        this.id = id;
        this.name = name;
        this.rol = rol;
        this.passwordHash = passwordHash;
        this.createAt = createAt;
        this.perfilImg = perfilImg;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRol() {
        return rol;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public String getPerfilImg() {
        return perfilImg;
    }
}
