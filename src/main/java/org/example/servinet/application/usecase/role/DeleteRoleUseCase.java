package org.example.servinet.application.usecase.role;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.LimitedDataRepository;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;

import java.util.List;


public class DeleteRoleUseCase {

    private final LimitedDataRepository<Role, List<Role>> roleSqlRepository;
    private final InMemoryRepository<Role> roleInMemoryRepository;
    private final StaffRepository sqlStaffAuthRepository;
    private final ActivityLogsService activityLogsService;
    private final AuthorizationSecurity authorizationService;

    public DeleteRoleUseCase(LimitedDataRepository<Role, List<Role>> roleSqlRepository, InMemoryRepository<Role> roleInMemoryRepository, StaffRepository sqlStaffAuthRepository, ActivityLogsService activityLogsService, AuthorizationSecurity authorizationService) {
        this.roleSqlRepository = roleSqlRepository;
        this.roleInMemoryRepository = roleInMemoryRepository;
        this.sqlStaffAuthRepository = sqlStaffAuthRepository;
        this.activityLogsService = activityLogsService;
        this.authorizationService = authorizationService;
    }


    public String delete(Role rol) {
        if (!authorizationService.hasPermission(Permission.APP_CONF_ROL_PERMS)) {
            return "No tienes permiso para gestionar roles.";
        }
        if (rol.hasPermission(Permission.BYPASS)) {
            return "El rol del Dueño no se puede eliminar.";
        }
        if (sqlStaffAuthRepository.countStaffWithRole(rol.getUuid()) > 0) {
            return "No puedes eliminar un rol que tiene usuarios asignados.";
        }
        roleSqlRepository.delete(rol.getUuid());
        roleInMemoryRepository.remove(rol.getUuid());

        activityLogsService.addLog(LogType.REMOVE_ROLE, "Eliminó el rol " + rol.getName());
        return "";
    }

}
