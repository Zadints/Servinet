package org.example.servinet.core.application.startapp;

import org.example.servinet.core.application.dto.RoleDto;
import org.example.servinet.core.application.dto.UserDto;
import org.example.servinet.core.application.usecase.*;
import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.exception.InvalidCredentialsException;
import org.example.servinet.core.domain.utils.GetHadware;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StartAppManager {

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


    /*
     * Métodos para crear el usuario base llamado Dueño.
     * el cual tiene permiso BYPASS y los roles base:
     * dueño
     * administrador
     * técnico
     * vendedor
     *
     */
    private static Set<Permission> setPermissions(Permission... perms) {
        return new HashSet<>(Arrays.asList(perms));
    }
    private static void createDefaultRol() {

        String response = null;

        //OWER
        response = RolesUseCase.createRol(
                new RoleDto(
                    setPermissions(
                            Permission.BYPASS),

                    "#540010",
                    "Owner"
        ));
        //ADMIN
        response = RolesUseCase.createRol(
                new RoleDto(
                        setPermissions(
                                Permission.DASH_BYPASS,
                                Permission.MY_PROFILE_INFO,
                                Permission.AD_BYPASS,
                                Permission.AD_PERSONAL_SECTION,
                                Permission.AD_ACTIVITY_PERSONAL_SECTION,
                                Permission.ANNOUNCE_CREATE,
                                Permission.ANNOUNCE_VIEW,
                                Permission.ANNOUNCE_DELETE,
                                Permission.ANT_BYPASS,
                                Permission.CLIENTS_BYPASS,
                                Permission.BACKUPS_BYPASS,
                                Permission.CLIENTS_BYPASS,
                                Permission.SELL_BYPASS,
                                Permission.ANNOUNCE_VIEW
                        ),
                        "#FE0032",
                        "Admin"
                ));
        //TECHNICIAN
        response = RolesUseCase.createRol(
                new RoleDto(
                        setPermissions(
                                Permission.TECHNICIAN_BYPASS,
                                Permission.MY_PROFILE_INFO,
                                Permission.ANT_VIEW_INFO,
                                Permission.ANT_VIEW_ANTENNAS,
                                Permission.ANT_START_MAINTE,
                                Permission.ANT_END_MAINT,
                                Permission.ANT_GO_ACTIVE,
                                Permission.ANT_GO_DESACTIVE,
                                Permission.ANNOUNCE_VIEW
                        ),
                        "#FE0032",
                        "Técnico"
                ));

        //SALE
        response = RolesUseCase.createRol(
                new RoleDto(
                        setPermissions(
                                Permission.SELL_BYPASS,
                                Permission.MY_PROFILE_INFO,
                                Permission.ANNOUNCE_VIEW
                        ),
                        "#FE0032",
                        "Vendedor"
                ));

        if (response == null){
            RolesUseCase.loadRoles();
        }

    }
    private static void createBasicUserIfNotExist() throws InvalidCredentialsException {

        Role roleOwner = RolesUseCase.getRol("Owner");
        SessionUseCase.registerBasicUser(
                new UserDto(
                        "Augusto",
                        "Cesar2014abc.",
                        roleOwner,
                        "tester@gmail.com",
                        null
                ));
    }


    public static void loadCacheApp() {

        LoadDb.startConnection();
        System.out.println("-----------------------------");
        try {

            System.out.println("Ccreando rol si no existe...");
            createDefaultRol();
            System.out.println("Creando usuario base si n oexiste...");
            createBasicUserIfNotExist();
        } catch (DatabaseException | InvalidCredentialsException  e ) {
            System.out.println(e.getMessage());
        }
        System.out.println("Cargando los Roles...");
        RolesUseCase.loadRoles();
        try {

            System.out.println("Ccreando rol si no existe...");
            createDefaultRol();
            System.out.println("Creando usuario base si n oexiste...");
            createBasicUserIfNotExist();
        } catch (DatabaseException | InvalidCredentialsException  e ) {
            System.out.println(e.getMessage());
        }
        System.out.println("Cargando personal...");
        SessionUseCase.loadAllUsers();
        System.out.println("Cargando configuraciòn de la app...");
        AppGeneralUseCase.loadAppConfig();
        System.out.println("Cargando los Clientes...");
        //ClientsUseCase.loadClients();
        System.out.println("Cargando las antenas..");
        AntennasUseCase.loadAntennas();

    }





}
