package org.example.servinet.application.startapp;

import org.example.servinet.application.dto.RoleDto;
import org.example.servinet.application.dto.UserDto;
import org.example.servinet.application.usecase.*;
import org.example.servinet.application.usecase.role.CreateRoleUseCase;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.exception.InvalidCredentialsException;
import org.example.servinet.infrastructure.hardware.WindowsHardwareIdProvider;

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
        return LoginStaffUseCase.automaticStaffLogin(WindowsHardwareIdProvider.id());
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
        response = CreateRoleUseCase.createRol(
                new RoleDto(
                    setPermissions(
                            Permission.BYPASS),

                    "#540010",
                    "Owner"
        ));
        //ADMIN
        response = CreateRoleUseCase.createRol(
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
        response = CreateRoleUseCase.createRol(
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
        response = CreateRoleUseCase.createRol(
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
            CreateRoleUseCase.loadRoles();
        }

    }
    private static void createBasicUserIfNotExist() throws InvalidCredentialsException {

        Role roleOwner = CreateRoleUseCase.getRol("Owner");
        LoginStaffUseCase.registerStaff(
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
        CreateRoleUseCase.loadRoles();
        try {

            System.out.println("Ccreando rol si no existe...");
            createDefaultRol();
            System.out.println("Creando usuario base si n oexiste...");
            createBasicUserIfNotExist();
        } catch (DatabaseException | InvalidCredentialsException  e ) {
            System.out.println(e.getMessage());
        }
        System.out.println("Cargando personal...");
        LoginStaffUseCase.loadAllUsers();
        System.out.println("Cargando configuraciòn de la app...");
        AppGeneralUseCase.loadAppConfig();

        try {
            System.out.println("Cargando los planes");
            PlanUseCase.loadAllPlans();
            System.out.println("Cargando los Clientes...");
            ClientsUseCase.loadClientsAndOrders();
            System.out.println("Procesando y separando staff y pedidos...");
            TechnicalUseCase.automaticTechniciansLoad();
        }catch (Exception e) {
            System.out.println(e.getCause());
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
        }
        System.out.println("Cargando las antenas..");
        AntennasUseCase.loadAntennas();

    }





}
