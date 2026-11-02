package org.example.servinet.application.usecase.staff;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import javafx.concurrent.Task;
import org.example.servinet.App;
import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.PasswordGenerator;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.service.WhatsAppService;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.ApiException;
import org.example.servinet.domain.exception.InvalidCredentialsException;
import org.example.servinet.domain.exception.PermissionException;

public class EditStaffUseCase {


    private final StaffRepository sqlStaffAuthRepository;
    private final InMemoryComplexRepository<Staff> inMemoryRepository;
    private final Argon2 argon;
    private final StaffCacheService staffCacheService;
    private final PasswordGenerator passwordGenerator;
    private final ActivityLogsService activityLogsService;
    private final AppExecutor appExecutor;
    private final WhatsAppService whatsAppService;

    public EditStaffUseCase(StaffRepository sqlStaffAuthRepository, InMemoryComplexRepository<Staff> inMemoryRepository, StaffCacheService staffCacheService, PasswordGenerator passwordGenerator, ActivityLogsService activityLogsService, AppExecutor appExecutor, WhatsAppService whatsAppService) {
        this.sqlStaffAuthRepository = sqlStaffAuthRepository;
        this.inMemoryRepository = inMemoryRepository;
        this.argon = Argon2Factory.create();
        this.staffCacheService = staffCacheService;
        this.passwordGenerator = passwordGenerator;
        this.activityLogsService = activityLogsService;
        this.appExecutor = appExecutor;
        this.whatsAppService = whatsAppService;
    }


    public void editUser(
            Staff target,
            String email,
            Role rol,
            String reloadPassword,
            byte[] imageBytes
    ) {

        boolean edit = false;

        if (email != null && !email.isBlank()) {

            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                throw new IllegalArgumentException(
                        "El correo ingresado no es válido"
                );
            }

            if (!email.equalsIgnoreCase(target.getEmail())) {

                if (sqlStaffAuthRepository.isNameOrEmailTaken(email)) {
                    throw new InvalidCredentialsException(
                            "Ya existe otro usuario con ese correo"
                    );
                }

                edit = true;

            } else {
                email = null;
            }

        } else {
            email = null;
        }



        String roleId = null;

        if (rol != null) {

            if (!rol.getUuid().equalsIgnoreCase(target.getRol().getUuid())) {

                boolean isMe = inMemoryRepository.getCurrent() != null
                        && inMemoryRepository.getCurrent()
                        .getUuid()
                        .equalsIgnoreCase(target.getUuid());

                if (isMe) {
                    throw new IllegalArgumentException(
                            "No puedes cambiar tu propio rol."
                    );
                }

                roleId = rol.getUuid();
                edit = true;
            }
        }



        String newHash = null;

        if (reloadPassword != null && !reloadPassword.isBlank()) {

            newHash = hashPassword(
                    passwordGenerator.generate(9)
            );

            edit = true;
        }



        if (imageBytes != null && imageBytes.length > 0) {
            edit = true;
        } else {
            imageBytes = null;
        }


        if (!edit) {
            return;
        }



        sqlStaffAuthRepository.updateStaff(
                target.getUuid(),
                email,
                roleId,
                newHash,
                imageBytes
        );

        activityLogsService.addLog(
                LogType.USER_EDIT,
                "Editó al usuario " + target.getDisplay()
        );
        Task<Void> task = new Task<>() {

            @Override
            protected Void call() throws Exception {

                inMemoryRepository.setCurrent(
                        sqlStaffAuthRepository.getStaffByUuid(target.getUuid())
                );

                staffCacheService.reload();

                Staff current = staffCacheService.getCurrent();

                try {
                    whatsAppService.sendMessage(
                            target.getPhone(),
                            """
                            🤖 *SERVINET BOT*
    
                            👋 ¡Hola! Tu cuenta ha sido actualizada por
                            el %s %s!
    
                            Gracias por trabajar con nosotros. 🚀
                            """.formatted(
                                    current.getRolName(),
                                    current.getDisplay()
                            )
                    );

                } catch (ApiException e) {
                    System.out.println(e.getMessage());
                }

                return null;
            }
        };

        appExecutor.execute(task);
    }
    private String hashPassword(String password){
        return argon.hash(3, 65536, 1, password);
    }
}
