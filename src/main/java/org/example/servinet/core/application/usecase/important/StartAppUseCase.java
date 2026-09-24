package org.example.servinet.core.application.usecase.important;

import org.example.servinet.core.application.dto.RoleDto;
import org.example.servinet.core.application.dto.UserDto;
import org.example.servinet.core.application.usecase.RolesUseCase;
import org.example.servinet.core.application.usecase.SessionUseCase;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.InvalidCredentialsException;
import org.example.servinet.core.domain.utils.GetHadware;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class StartAppUseCase {

    private static boolean alreadyStartApp;

    public static boolean isAlreadyStartApp() {
        return alreadyStartApp;
    }

    public static void setAlreadyStartApp() {
        alreadyStartApp = true;
    }

    public static boolean checkAndConfigureDatabase(){
        return true;
    }

    public static boolean checkSessionActive() {
        return SessionUseCase.automaticUserLogin(GetHadware.id());
    }

    private static void createBasicRolIfNotExist() {
        Set<Permission> p = new HashSet<Permission>();
        p.add(Permission.BYPASS);

        RolesUseCase.createRol(new RoleDto(
                p,"#22A5F1", "Dueño"
        ));
    }

    private static void createBasicUserIfNotExist() {
        Path path = Path.of("D:/inglés/foto.jpg");

        Role uuid = RolesUseCase.getRol("Dueño");

        System.out.println(uuid.getUuid());

        try {
            SessionUseCase.registerUser(new UserDto(
                    "Augusto",
                    "Cesar2014abc.",
                    uuid,
                    "tester@gmail.com",
                    path
            ));
        } catch (InvalidCredentialsException e){

        }


    }

    public static boolean loadAllConfigApp() {
        createBasicRolIfNotExist();
        createBasicUserIfNotExist();
        return true;
    }
}
