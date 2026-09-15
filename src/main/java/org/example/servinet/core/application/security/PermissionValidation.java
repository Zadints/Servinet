package org.example.servinet.core.application.security;

import org.example.servinet.core.application.usecase.LogsUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.LogType;
import org.example.servinet.core.domain.enums.Permission;

public class PermissionValidation {
    private static boolean isBypassUser;

    public void loadPermission(){
        Role tempRol = SessionUseCase.getUserRol();
        if (tempRol.hasPermission(Permission.BYPASS)){
            isBypassUser = true;
            return;
        }
        isBypassUser = false;
    }

    public static boolean hasPermission(Permission p){
        if (isBypassUser) return true;
        Role tempRol = SessionUseCase.getUserRol();
        return tempRol.hasPermission(p);
    }

    public static boolean addPermission(Permission p){
        Role tempRol = SessionUseCase.getUserRol();
        LogsUseCase.addLog(LogType.PERM_ADD);
        return tempRol.addPermissions(p);
    }

    public static void removePermission(Permission p){
        Role tempRol = SessionUseCase.getUserRol();
        tempRol.removePermission(p);
    }

}
