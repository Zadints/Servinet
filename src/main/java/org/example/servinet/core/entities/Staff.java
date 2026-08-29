package org.example.servinet.core.entities;

import org.example.servinet.core.enums.Role;

import java.time.LocalDateTime;

//Si vas a editar entidad también edita SQL SERVER

public class Staff {
    private String staffId;
    private String staffName;
    private Role rol;
    private String passwordHash;
    private LocalDateTime createAt;
    private String perfilImg;


    public Staff(String staffId, String staffName, Role rol, String passwordHash, LocalDateTime createAt, String perfilImg) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.rol = rol;
        this.passwordHash = passwordHash;
        this.createAt = createAt;
        this.perfilImg = perfilImg;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
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
