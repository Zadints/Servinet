package org.example.servinet.application.usecase.staff;

import com.sun.jdi.InvalidTypeException;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import javafx.concurrent.Task;
import org.example.servinet.application.dto.database.AuthLogin;
import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.exception.InvalidCredentialsException;
import org.example.servinet.application.port.out.HardwareIdProvider;


public class LoginStaffUseCase {
    private final HardwareIdProvider hardwareIdProvider;
    private final StaffRepository sqlStaffAuthRepository;
    private final InMemoryComplexRepository<Staff> inMemoryRepository;
    private final Argon2 argon;
    private final ActivityLogsService activityLogsService;
    private final StaffCacheService staffCacheService;
    private final AppExecutor appExecutor;
    private final AuthorizationSecurity authorizationService;

    public LoginStaffUseCase(HardwareIdProvider hardwareIdProvider, StaffRepository sqlStaffAuthRepository, InMemoryComplexRepository<Staff> staffRepository, ActivityLogsService activityLogsService, StaffCacheService staffCacheService, AppExecutor appExecutor, AuthorizationSecurity authorizationService) {
        this.hardwareIdProvider = hardwareIdProvider;
        this.sqlStaffAuthRepository = sqlStaffAuthRepository;
        this.inMemoryRepository = staffRepository;
        this.activityLogsService = activityLogsService;
        this.staffCacheService = staffCacheService;
        this.appExecutor = appExecutor;
        this.authorizationService = authorizationService;
        this.argon = Argon2Factory.create();
    }


    public boolean automaticStaffLogin(){

        String hardwareId = hardwareIdProvider.getId();

        if (hardwareId.isEmpty()) return false;



        String staffUuid = sqlStaffAuthRepository.getStaffActiveHardwareId(hardwareId);

        Staff staffAccount = null;

        if (staffUuid != null){
            staffAccount = sqlStaffAuthRepository.getStaffAndNewSession(hardwareId, staffUuid);
        }

        if (staffAccount == null) return false;

        inMemoryRepository.setCurrent(staffAccount);

        authorizationService.setAssignedPolicy(inMemoryRepository.getCurrent().getRol().getPermissions());
        staffCacheService.reload();
        activityLogsService.addLog(LogType.LOGIN, "Inicio de sesión");
        return true;
    }

    public boolean loginStaff(String email, String password){


        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException("Debes ingresar un nombre de usuario");
        }

        if (password == null || password.isBlank()) {
            throw new InvalidCredentialsException("Debes ingresar una contraseña");
        }

        AuthLogin dto = sqlStaffAuthRepository.getStaffAuthLoginByEmail(email);

        if (dto == null){
            throw new InvalidCredentialsException(
                    "Usuario o contraseña incorrectos"
            );
        }

        if (!comparePassword(dto.passwordHash(), password)){
            return false;
        }

        Staff staffAccount = sqlStaffAuthRepository.getStaffAndNewSession(hardwareIdProvider.getId(), dto.userUuid());

        staffCacheService.reload();
        inMemoryRepository.setCurrent(staffAccount);
        activityLogsService.addLog(LogType.LOGIN, "Inicio de sesión");


        return true;

    }

    private boolean comparePassword(String pasword, String newPassword) {
        return argon.verify(pasword, newPassword);
    }


    public void closeActualSessionStaff() {

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                sqlStaffAuthRepository.removeStaffSession(hardwareIdProvider.getId());
                try {
                    inMemoryRepository.resetCurrent();
                } catch (InvalidTypeException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };

        appExecutor.execute(task);

    }









}
