package org.example.servinet.application.security;

import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.UiPermission;
import org.example.servinet.domain.enums.ModuleApp;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;

import java.util.Set;

public class AuthorizationSecurity {

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    private final PermissionPolicy policy;
    private UiPermission uiPermission;
    private boolean setPolicy;

    public AuthorizationSecurity(InMemoryComplexRepository<Staff> staffInMemoryRepository, PermissionPolicy permissionPolicy) {
        this.staffInMemoryRepository = staffInMemoryRepository;
        this.policy = permissionPolicy;
        setPolicy = false;
    }

    public void setAssignedPolicy(Set<Permission> assigned){
        if (setPolicy){
            throw new PermissionException("No peudes asignar 2 veces la misma política");
        }
        policy.setAssignedPolicy(assigned);
        loadPermissionForPolicy();
        this.setPolicy = true;
    }


    private void loadPermissionForPolicy(){
        uiPermission = new UiPermission(
                policy.canOpen(ModuleApp.DASHBOARD),
                policy.canOpen(ModuleApp.ADMINISTRATION),
                policy.canOpen(ModuleApp.ANNOUNCEMENTS),
                policy.canOpen(ModuleApp.ANTENNAS),
                policy.canOpen(ModuleApp.SALES),
                policy.canOpen(ModuleApp.BACKUPS),
                policy.canOpen(ModuleApp.CLIENTS),
                policy.canOpen(ModuleApp.TECHNICIANS)
        );
    }
    public UiPermission getUiPermission() {
        return uiPermission;
    }

    public boolean hasPermission(Permission p) {

        if (!setPolicy){
            throw new PermissionException("No se a establecido aún una política de permiso");
        }

        return policy.allows(p);
    }

    public boolean hasPermission(Permission... p) {

        if (!setPolicy){
            throw new PermissionException("No se a establecido aún una política de permiso");
        }

        return policy.allowsAny(p);
    }

    public boolean isIsBypassUser() {

        return hasPermission(Permission.BYPASS);
    }

    public boolean addPermission(Permission p) {

        if (p == null) {
            throw new PermissionException("El permiso ingresado es null");
        }

        if (!hasPermission(Permission.APP_CONF_ROL_PERMS)) {
            return false;
        }

        if (p == Permission.BYPASS
                && !hasPermission(Permission.BYPASS)) {
            return false;
        }



        Role currentRole = staffInMemoryRepository.getCurrent().getRol();

        if (currentRole == null) {
            return false;
        }
        currentRole.addPermissions(p);
        loadPermissionForPolicy();
        return true;
    }

    public void removePermission(Permission p){
        Role tempRol = staffInMemoryRepository.getCurrent().getRol();
        tempRol.removePermission(p);
    }



}
