package org.example.servinet.domain.entities;

import javafx.scene.image.Image;
import org.example.servinet.domain.repository.Identifiable;

import java.time.LocalDateTime;

public class Staff implements Identifiable {
    private final String uuid;
    private String display;
    private String phone;
    private final String email;
    private final Role rol;
    private final LocalDateTime createAt;
    private byte[] perfilImg;

    public Staff(String uuid, String phone, String email, Role rol, LocalDateTime createAt, String display, byte[] perfilImg) {
        this.uuid = uuid;
        this.phone = phone;
        this.email = email;
        this.rol = rol;
        this.createAt = createAt;
        this.display = display;
        this.perfilImg = perfilImg;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
    public void setPerfilImg(byte []perfilImg) {
        this.perfilImg = perfilImg;
    }

    public String getPhone() {
        return phone;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDisplay() {
        return display;
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

    public byte[] getPerfilImg() {
        return perfilImg;
    }


    @Override
    public String toString() {
        return display;
    }
}
