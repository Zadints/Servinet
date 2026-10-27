package org.example.servinet.application.usecase.staff;

import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;

public class DeleteStaffUseCase {

    private final StaffRepository sqlStaffAuthRepository;
    private final InMemoryComplexRepository<Staff> inMemoryRepository;

    private final StaffCacheService staffCacheService;
    private final AuthorizationSecurity authorizationService;
    private final ActivityLogsService activityLogsService;

    public DeleteStaffUseCase(StaffRepository sqlStaffAuthRepository, InMemoryComplexRepository<Staff> inMemoryRepository, StaffCacheService staffCacheService, AuthorizationSecurity authorizationService, ActivityLogsService activityLogsService) {
        this.sqlStaffAuthRepository = sqlStaffAuthRepository;
        this.inMemoryRepository = inMemoryRepository;
        this.staffCacheService = staffCacheService;
        this.authorizationService = authorizationService;
        this.activityLogsService = activityLogsService;
    }

    public void deleteUser(Staff target) {
        if (!authorizationService.hasPermission(Permission.AD_DELETE_PERSONAL)) {
            throw new PermissionException("No tienes permiso para eliminar usuarios");
        }

        Staff currentStaff = inMemoryRepository.getCurrent();

        if (currentStaff != null && currentStaff.getUuid().equalsIgnoreCase(target.getUuid())) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }
        if (target.getRol().hasPermission(Permission.BYPASS)) {
            throw new IllegalArgumentException("No se puede eliminar a un usuario con rol de Dueño.");
        }
        sqlStaffAuthRepository.deleteStaff(target.getUuid());
        activityLogsService.addLog(LogType.USER_DELETE, "Eliminó al usuario " + target.getDisplay());
        staffCacheService.reload();
    }
}
