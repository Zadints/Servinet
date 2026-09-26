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

        RoleModel.addRoleDatabase(
                newRole,
                perms

        );

        roles.add(newRole);
        return "";
    }

    public static void loadRoles(){
        roles = RoleModel.getRolesDatabase();
    }


    public static List<Role> getRoles() {
        return roles;
    }
    public static Role getRol(String name) {

        if (name == null) {
            return null;
        }

        for (Role rol : roles) {

            if (rol.getName() != null
                    && rol.getName().equalsIgnoreCase(name.trim())) {

                return rol;
            }
        }

        return null;
    }

    public static String createRolFromAdmin(RoleDto dto) {

        if (!PermissionValidation.hasPermission(
                Permission.APP_CONF_ROL_PERMS
        )) {

            return "No tienes permiso para gestionar roles.";
        }


        if (dto == null) {
            return "Los datos del rol no son válidos.";
        }


        if (dto.getPermissions() != null
                && dto.getPermissions().contains(Permission.BYPASS)
                && !PermissionValidation.hasPermission(Permission.BYPASS)) {

            return "Solo el Dueño puede crear roles con BYPASS.";
        }


        if (getRol(dto.getName()) != null) {
            return "Ya existe un rol con ese nombre.";
        }


        String error = createRol(dto);


        if (error.isEmpty()) {

            LogsUseCase.addLog(
                    LogType.ADD_ROLE,
                    "Creó el rol " + dto.getName()
            );
        }


        return error;
    }

    public static String updateRolFromAdmin(
            Role role,
            RoleDto dto
    ) {

        /*
         * ============================
         * VALIDACIONES GENERALES
         * ============================
         */

        if (!PermissionValidation.hasPermission(
                Permission.APP_CONF_ROL_PERMS
        )) {

            return "No tienes permiso para gestionar roles.";
        }


        if (role == null) {
            return "El rol seleccionado no es válido.";
        }


        if (dto == null) {
            return "Los datos del rol no son válidos.";
        }


        String newName = dto.getName();
        String newColor = dto.getHexColor();
        Set<Permission> newPermissions =
                dto.getPermissions();


        /*
         * ============================
         * VALIDAR NOMBRE
         * ============================
         */

        if (newName == null || newName.isBlank()) {
            return "El nombre del rol es obligatorio.";
        }


        /*
         * ============================
         * VALIDAR COLOR
         * ============================
         */

        if (newColor == null || newColor.isBlank()) {
            return "El color del rol es obligatorio.";
        }


        if (!newColor.matches(
                "^#[A-Fa-f0-9]{6}$"
        )) {

            return "El color debe tener formato HEX válido. Ejemplo: #00A6FF";
        }


        /*
         * ============================
         * VALIDAR PERMISOS
         * ============================
         */

        if (newPermissions == null
                || newPermissions.isEmpty()) {

            return "El rol debe tener al menos un permiso.";
        }


        /*
         * ============================
         * EVITAR NOMBRES DUPLICADOS
         * ============================
         */

        Role duplicated = findRoleByNameExcept(
                newName,
                role.getUuid()
        );

        if (duplicated != null) {
            return "Ya existe otro rol con ese nombre.";
        }


        /*
         * ============================
         * PROTECCIÓN BYPASS
         * ============================
         */

        boolean roleCurrentlyHasBypass =
                role.hasPermission(Permission.BYPASS);

        boolean newRoleHasBypass =
                newPermissions.contains(Permission.BYPASS);


        /*
         * Nadie que no sea BYPASS puede
         * otorgar ese permiso.
         */
        if (newRoleHasBypass
                && !PermissionValidation.hasPermission(
                Permission.BYPASS
        )) {

            return "Solo el Dueño puede asignar el permiso BYPASS.";
        }


        /*
         * Protegemos el rol principal.
         *
         * Si el rol ya tenía BYPASS,
         * no permitimos quitarlo.
         */
        if (roleCurrentlyHasBypass
                && !newRoleHasBypass) {

            return "No puedes quitar BYPASS al rol del Dueño.";
        }


        /*
         * ============================
         * ACTUALIZAR OBJETO
         * ============================
         */

        role.setName(
                newName.trim()
        );

        role.setHexColor(
                newColor.trim()
        );

        role.setPermissions(
                newPermissions
        );


        /*
         * ============================
         * ACTUALIZAR BD
         * ============================
         */

        RoleModel.updateRoleDatabase(
                role,
                newPermissions
        );


        /*
         * ============================
         * LOG
         * ============================
         */

        LogsUseCase.addLog(
                LogType.EDIT_ROLE,
                "Editó el rol " + role.getName()
        );


        return "";
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

    private static Role findRoleByNameExcept(
            String name,
            String ignoredUuid
    ) {

        if (name == null) {
            return null;
        }

        for (Role role : roles) {

            if (role.getUuid().equals(ignoredUuid)) {
                continue;
            }

            if (role.getName() != null
                    && role.getName().equalsIgnoreCase(name.trim())) {

                return role;
            }
        }

        return null;
    }
}
