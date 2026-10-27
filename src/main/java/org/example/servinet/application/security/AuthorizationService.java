package org.example.servinet.application.security;

import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.infrastructure.database.sqlserver.SqlStaffAuthRepository;

import java.util.Set;

public class AuthorizationService {

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    private final PermissionPolicy permissionPolicy;
    private boolean setPolicy;

    public AuthorizationService(InMemoryComplexRepository<Staff> staffInMemoryRepository, PermissionPolicy permissionPolicy) {
        this.staffInMemoryRepository = staffInMemoryRepository;
        this.permissionPolicy = permissionPolicy;
        setPolicy = false;
    }

    public void setAssignedPolicy(Set<Permission> assigned){
        if (setPolicy){
            throw new PermissionException("No peudes asignar 2 veces la misma política");
        }
        this.setPolicy = true;
    }


    public boolean hasPermission(Permission p) {

        if (!setPolicy){
            throw new PermissionException("No se a establecido aún una política de permiso");
        }

        return permissionPolicy.allows(p);
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

        return true;
    }

    public void removePermission(Permission p){
        Role tempRol = staffInMemoryRepository.getCurrent().getRol();
        tempRol.removePermission(p);
    }

}
