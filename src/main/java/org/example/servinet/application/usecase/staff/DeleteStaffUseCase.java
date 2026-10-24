package org.example.servinet.application.usecase.staff;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.application.security.PermissionValidation;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.RoleNoPermission;

public class DeleteStaffUseCase {

    private final StaffRepository sqlStaffAuthRepository;
    private final InMemoryRepository<Staff> inMemoryRepository;
    private final StaffCacheService staffCacheService;

    public DeleteStaffUseCase(StaffRepository sqlStaffAuthRepository, InMemoryRepository<Staff> inMemoryRepository, StaffCacheService staffCacheService) {
        this.sqlStaffAuthRepository = sqlStaffAuthRepository;
        this.inMemoryRepository = inMemoryRepository;
        this.staffCacheService = staffCacheService;
    }

    public void deleteUser(Staff target) {
        if (!PermissionValidation.hasPermission(Permission.AD_DELETE_PERSONAL)) {
            throw new RoleNoPermission("No tienes permiso para eliminar usuarios");
        }

        Staff currentStaff = inMemoryRepository.getCurrent();

        if (currentStaff != null && currentStaff.getUuid().equalsIgnoreCase(target.getUuid())) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }
        if (target.getRol().hasPermission(Permission.BYPASS)) {
            throw new IllegalArgumentException("No se puede eliminar a un usuario con rol de Dueño.");
        }
        sqlStaffAuthRepository.deleteStaff(target.getUuid());
        ActivityLogsService.addLog(LogType.USER_DELETE, "Eliminó al usuario " + target.getDisplay());
        staffCacheService.reload();
    }
}
