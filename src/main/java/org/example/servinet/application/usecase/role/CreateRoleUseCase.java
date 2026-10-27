package org.example.servinet.application.usecase.role;

import org.example.servinet.application.dto.RoleDto;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlRepository;
import org.example.servinet.application.security.AuthorizationService;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.GenerateIdUseCase;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;

import java.util.Set;
import java.util.UUID;

public class CreateRoleUseCase {

    private final SqlRepository<Role> roleSqlRepository;
    private final InMemoryRepository<Role> roleInMemoryRepository;

    private final AuthorizationService authorizationService;
    private final ActivityLogsService activityLogsService;

    public CreateRoleUseCase(SqlRepository<Role> roleSqlRepository, InMemoryRepository<Role> roleInMemoryRepository, AuthorizationService authorizationService, ActivityLogsService activityLogsService) {

        this.roleSqlRepository = roleSqlRepository;
        this.roleInMemoryRepository = roleInMemoryRepository;
        this.authorizationService = authorizationService;
        this.activityLogsService = activityLogsService;
    }

    public String createRol(RoleDto dto){

        String name = dto.getName();
        String color = dto.getHexColor();
        Set<Permission> perms = dto.getPermissions();

        if (name == null || name.isBlank()) {
            return "El nombre del dto es obligatorio.";
        }

        if (color == null || color.isBlank()) {
            return "El color del dto es obligatorio.";
        }

        if (!color.matches("^#[A-Fa-f0-9]{6}$")) {
            return "El color debe tener un formato HEX válido. Ejemplo: #00A6FF";
        }

        if (perms == null || perms.isEmpty()) {
            return "El dto debe tener al menos un permiso.";
        }

        for (Role r: roleInMemoryRepository.getAll()){
           if(r.getName().equalsIgnoreCase(name)){
               return "El dto ya está registrado";
           }
        }

        String uuid = UUID.randomUUID().toString();

        Role newRole = new Role(
                uuid,
                perms,
                color,
                name
        );

        roleSqlRepository.add(newRole);
        roleInMemoryRepository.addEntity(newRole);
        return "";
    }




    public String createRolFromAdmin(RoleDto dto) {

        if (!authorizationService.hasPermission(
                Permission.APP_CONF_ROL_PERMS
        )) {

            return "No tienes permiso para gestionar roles.";
        }


        if (dto == null) {
            return "Los datos del rol no son válidos.";
        }


        if (dto.getPermissions() != null
                && dto.getPermissions().contains(Permission.BYPASS)
                && !authorizationService.hasPermission(Permission.BYPASS)) {

            return "Solo el Dueño puede crear roles con BYPASS.";
        }


        for (Role r : roleInMemoryRepository.getAll()){
            if (r.getName().equals(dto.getName())) {
                return "Ya existe un rol con ese nombre.";
            }
        }


        String error = createRol(dto);


        if (error.isEmpty()) {

            activityLogsService.addLog(
                    LogType.ADD_ROLE,
                    "Creó el rol " + dto.getName()
            );
        }


        return error;
    }





}
