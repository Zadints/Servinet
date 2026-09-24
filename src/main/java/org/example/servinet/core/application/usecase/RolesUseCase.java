package org.example.servinet.core.application.usecase;

import org.example.servinet.core.application.dto.RoleDto;
import org.example.servinet.core.application.service.UuidGenerator;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.infrastructure.database.models.RoleModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RolesUseCase {
    private static List<Role> roles = new ArrayList<Role>();

    public static String createRol(RoleDto rol){
        String name = rol.getName();
        String color = rol.getHexColor();
        Set<Permission> perms = rol.getPermissions();

        if (name == null || name.isBlank()) {
            return "El nombre del rol es obligatorio.";
        }

        if (color == null || color.isBlank()) {
            return "El color del rol es obligatorio.";
        }

        if (!color.matches("^#[A-Fa-f0-9]{6}$")) {
            return "El color debe tener un formato HEX válido. Ejemplo: #00A6FF";
        }

        if (perms == null || perms.isEmpty()) {
            return "El rol debe tener al menos un permiso.";
        }

        String uuid = new GenerateIdUseCase(new UuidGenerator()).execute();
        System.out.println(uuid);
        Role newRole = new Role(
                uuid,
                perms,
                color,
                name
        );

        roles.add(newRole);
        RoleModel.addRoleDatabase(newRole, perms);
        return "";
    }

    public static void loadRoles(){
        roles = RoleModel.getRolesDatabase();
    }


    public static List<Role> getRoles() {
        return roles;
    }
    public static Role getRol(String name) {
        for (Role rol : roles) {
            if (rol.getName().equals(name)) {
                return rol;
            }
        }
        return null;
    }
}
