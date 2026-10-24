package org.example.servinet.application.security;

import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;

public class PermissionValidation {
    public void loadPermission(){
        // Se conserva por compatibilidad.
        // Los permisos se consultan desde la sesión actual.
    }

    public static boolean hasPermission(Permission p) {
        return PermissionPolicy.current().allows(p);
    }

    public static boolean isIsBypassUser() {
        return hasPermission(Permission.BYPASS);
    }

    public static boolean addPermission(Permission p) {

        if (p == null) {
            return false;
        }

        if (!hasPermission(Permission.APP_CONF_ROL_PERMS)) {
            return false;
        }

        // Solo un propietario puede conceder BYPASS global.
        if (p == Permission.BYPASS
                && !hasPermission(Permission.BYPASS)) {
            return false;
        }

        Role currentRole = LoginStaffUseCase.getUserRol();

        if (currentRole == null) {
            return false;
        }

        boolean added = currentRole.addPermissions(p);

        if (added) {
            ActivityLogsService.addLog(LogType.PERM_ADD);
        }

        return added;
    }

    public static void removePermission(Permission p){
        Role tempRol = LoginStaffUseCase.getUserRol();
        tempRol.removePermission(p);
    }

}
