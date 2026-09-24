package org.example.servinet.core.domain.entities;

import javafx.scene.image.Image;
import org.example.servinet.core.domain.repository.Identifiable;

import java.time.LocalDateTime;

public class User implements Identifiable {
    private final String uuid;
    private String name;
    private final String email;
    private final Role rol;
    private final String passwordHash;
    private final LocalDateTime createAt;
    private Image perfilImg;

    public User(String uuid, String email, Role rol, LocalDateTime createAt, String passwordHash, String name, Image perfilImg) {
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
    public void setPerfilImg(Image perfilImg) {
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

    public String getRolName() {
        return rol.getName();
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public Image getPerfilImg() {
        return perfilImg;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    @Override
    public String toString() {
        return name;
    }
}
