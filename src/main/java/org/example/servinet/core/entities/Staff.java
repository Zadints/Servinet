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
}
