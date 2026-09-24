package org.example.servinet.core.application.usecase;

import org.example.servinet.core.application.dto.RoleDto;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.application.service.UuidGenerator;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.LogType;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.infrastructure.database.models.RoleModel;
import org.example.servinet.infrastructure.database.models.UserModel;

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

    public static String createRolFromAdmin(RoleDto dto) {
        if (!PermissionValidation.hasPermission(Permission.APP_CONF_ROL_PERMS)) {
            return "No tienes permiso para gestionar roles.";
        }
        if (dto.getPermissions().contains(Permission.BYPASS)
                && !PermissionValidation.hasPermission(Permission.BYPASS)) {
            return "Solo el Dueño puede crear roles con BYPASS.";
        }
        if (getRol(dto.getName()) != null) {
            return "Ya existe un rol con ese nombre.";
        }
        String error = createRol(dto);
        if (error.isEmpty()) {
            LogsUseCase.addLog(LogType.ADD_ROLE, "Creó el rol " + dto.getName());
        }
        return error;
    }

    public static String deleteRol(Role rol) {
        if (!PermissionValidation.hasPermission(Permission.APP_CONF_ROL_PERMS)) {
            return "No tienes permiso para gestionar roles.";
        }
        if (rol.hasPermission(Permission.BYPASS)) {
            return "El rol del Dueño no se puede eliminar.";
        }
        if (UserModel.countUsersWithRole(rol.getUuid()) > 0) {
            return "No puedes eliminar un rol que tiene usuarios asignados.";
        }
        RoleModel.deleteRoleDatabase(rol.getUuid());
        roles.remove(rol);
        LogsUseCase.addLog(LogType.REMOVE_ROLE, "Eliminó el rol " + rol.getName());
        return "";
    }
}
