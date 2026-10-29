package org.example.servinet.application.usecase.staff;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.PasswordGenerator;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.FileExistException;
import org.example.servinet.domain.exception.InvalidCredentialsException;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.ui.ImageConverter;

import java.io.IOException;
import java.nio.file.Path;

public class EditStaffUseCase {


    private final StaffRepository sqlStaffAuthRepository;
    private final InMemoryComplexRepository<Staff> inMemoryRepository;
    private final Argon2 argon;
    private final StaffCacheService staffCacheService;
    private final PasswordGenerator passwordGenerator;

    private final ActivityLogsService activityLogsService;
    private final AuthorizationSecurity authorizationService;
    public EditStaffUseCase(StaffRepository sqlStaffAuthRepository, InMemoryComplexRepository<Staff> staffRepository, StaffCacheService staffCacheService, PasswordGenerator passwordGenerator, ActivityLogsService activityLogsService, AuthorizationSecurity authorizationService) {
        this.sqlStaffAuthRepository = sqlStaffAuthRepository;
        this.inMemoryRepository = staffRepository;
        this.staffCacheService = staffCacheService;
        this.passwordGenerator = passwordGenerator;
        this.activityLogsService = activityLogsService;
        this.authorizationService = authorizationService;
        this.argon = Argon2Factory.create();
    }

    public void editUser(Staff target, String name, String email, Role rol, Boolean reloadPassword, byte[] imageBytes) {
        if (!authorizationService.hasPermission(Permission.AD_EDIT_PERSONAL)) {
            throw new PermissionException("No tienes permiso para editar usuarios");
        }
        validateNameAndEmail(name, email);
        if (rol == null) {
            throw new IllegalArgumentException("Debes seleccionar un rol");
        }

        boolean isMe = inMemoryRepository.getCurrent() != null && inMemoryRepository.getCurrent().getUuid().equalsIgnoreCase(target.getUuid());
        if (isMe && !target.getRol().getUuid().equalsIgnoreCase(rol.getUuid())) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol.");
        }

        String newHash = null;
        if (reloadPassword) {
            newHash = hashPassword(passwordGenerator.generate(9));
        }

        if (sqlStaffAuthRepository.isNameOrEmailTaken(name, email)) {
            throw new InvalidCredentialsException("Ya existe otro usuario con ese nombre o correo");
        }

        sqlStaffAuthRepository.updateStaff(target.getUuid(), name, email, rol.getUuid(), newHash, imageBytes);
        activityLogsService.addLog(LogType.USER_EDIT, "Editó al usuario " + target.getDisplay());

        //aqui enviar a gmail el password newHash

        if (isMe) {
            inMemoryRepository.setCurrent(sqlStaffAuthRepository.getStaffByUuid(target.getUuid()));
        }
        staffCacheService.reload();
    }

    private  void validateNameAndEmail(String name, String email) {
        if (name == null || !name.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new IllegalArgumentException("El nombre solo puede contener letras y espacios.");
        }
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new IllegalArgumentException("El correo ingresado no es válido");
        }
    }

    private String hashPassword(String password){
        return argon.hash(3, 65536, 1, password);
    }
}
