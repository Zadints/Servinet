package org.example.servinet.core.application.security;

import org.example.servinet.core.application.usecase.LogsUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.LogType;
import org.example.servinet.core.domain.enums.Permission;

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

        Role currentRole = SessionUseCase.getUserRol();

        if (currentRole == null) {
            return false;
        }

        boolean added = currentRole.addPermissions(p);

        if (added) {
            LogsUseCase.addLog(LogType.PERM_ADD);
        }

        return added;
    }

    public static void removePermission(Permission p){
        Role tempRol = SessionUseCase.getUserRol();
        tempRol.removePermission(p);
    }

}
