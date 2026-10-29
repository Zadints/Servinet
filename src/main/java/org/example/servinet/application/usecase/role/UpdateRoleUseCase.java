package org.example.servinet.application.usecase.role;

import org.example.servinet.application.dto.RoleDto;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.LimitedDataRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;

import java.util.HashSet;
import java.util.Set;

public class UpdateRoleUseCase {

    private final LimitedDataRepository<Role, InMemoryRepository<Role>> roleSqlRepository;
    private final InMemoryRepository<Role> roleInMemoryRepository;
    private final ActivityLogsService activityLogsService;
    private final AuthorizationSecurity authorizationService;

    public UpdateRoleUseCase(LimitedDataRepository<Role, InMemoryRepository<Role>> roleSqlRepository, InMemoryRepository<Role> roleInMemoryRepository, ActivityLogsService activityLogsService, AuthorizationSecurity authorizationService) {
        this.roleSqlRepository = roleSqlRepository;
        this.roleInMemoryRepository = roleInMemoryRepository;
        this.activityLogsService = activityLogsService;
        this.authorizationService = authorizationService;
    }


    public void update(Role role, RoleDto dto) {

        if (!authorizationService.hasPermission(Permission.APP_CONF_ROL_PERMS)) {
            throw new PermissionException("UpdateUseCase - No tienes permiso para gestionar roles.");
        }


        if (role == null) {
            throw new PermissionException("UpdateUseCase - El rol ingresado es null");
        }


        if (dto == null) {
            throw new PermissionException("UpdateUseCase - El dto Rol es nulo");
        }


        String newName = dto.getName();
        String newColor = dto.getHexColor();
        Set<Permission> newPermissions =
                dto.getPermissions();


        if (newName == null || newName.isBlank()) {
            throw new PermissionException("UpdateUseCase - El nombre del rol es obligatorio.");
        }

        if (newColor == null || newColor.isBlank()) {
            throw new PermissionException("UpdateUseCase - El color del rol es obligatorio.");
        }

        if (!newColor.matches(
                "^#[A-Fa-f0-9]{6}$"
        )) {

            throw new PermissionException(
                    "UpdateUseCase -El color debe tener formato HEX válido. Ejemplo: #00A6FF");
        }

        if (newPermissions == null || newPermissions.isEmpty()) {
            throw new PermissionException(
                    "UpdateUseCase - El rol debe tener al menos un permiso.");
        }

        Role duplicated = findRoleByNameExcept(
                newName,
                role.getUuid()
        );

        if (duplicated != null) {
            throw new PermissionException("UpdateUseCase -Ya existe otro rol con ese nombre.");
        }

        boolean roleCurrentlyHasBypass = role.hasPermission(Permission.BYPASS);
        boolean newRoleHasBypass = newPermissions.contains(Permission.BYPASS);


        /*
         * Nadie que no sea BYPASS puede
         * otorgar ese permiso.
         */
        if (newRoleHasBypass && !authorizationService.hasPermission(Permission.BYPASS)) {
            throw new PermissionException("Solo el Dueño puede asignar el permiso BYPASS.");
        }
        /*
         * Protegemos el rol principal.
         * Si el rol ya tenía BYPASS,
         * no permitimos quitarlo.
         */
        if (roleCurrentlyHasBypass && !newRoleHasBypass) {
            throw new PermissionException("No puedes quitar BYPASS al rol del Dueño.");
        }
        /*
         * PREPARAR NUEVOS DATOS
         */

        String cleanName = newName.trim();
        String cleanColor = newColor.trim();

        Set<Permission> cleanPermissions =
                new HashSet<>(newPermissions);



        Role updatedRole = new Role(
                role.getUuid(),
                cleanPermissions,
                cleanColor,
                cleanName
        );


        /*
         * ACTUALIZAR BASE DE DATOS
         * Primero intentamos persistir.
         * Si SQL Server falla, el Role original
         * permanece intacto. ok?
         */

        roleSqlRepository.update(updatedRole);


        /*
         * ACTUALIZAR MEMORIA
         * Solo llegamos aquí si SQL Server
         * terminó correctamente.
         */

        role.setName(cleanName);

        role.setHexColor(cleanColor);

        role.setPermissions(cleanPermissions);

        /*
         * LOG
         */

        activityLogsService.addLog(
                LogType.EDIT_ROLE,
                "Editó el rol " + role.getName()
        );
    }


    private Role findRoleByNameExcept(
            String name,
            String ignoredUuid
    ) {

        if (name == null) {
            return null;
        }

        for (Role role : roleInMemoryRepository.getAll()) {

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
