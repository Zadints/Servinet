package org.example.servinet.domain.enums;

public enum LogType {
    /*------------- Sesión ---------------*/
    LOGIN,
    LOGOUT,
    /*------------- Usuarios ---------------*/
    USER_CREATE,
    USER_EDIT,
    USER_DELETE,
    PASSWORD_CHANGE,
    /*------------- Rol ---------------*/
    ADD_ROLE,
    REMOVE_ROLE,
    EDIT_ROLE,
    /*------------- Permission ---------------*/
    PERM_ADD,
    PERM_REMOVE,
    /*------------- Aplicación ---------------*/
    APP_RENAME,
    /*------------- Tecnicos ---------------*/
    HOME_INSTALLED,
    ORDER_ACCEPTED,
    ORDER_CANCELLED,
    /*------------- Annuncios ---------------*/
    ANNOUNCE_CREATE,
    ANNOUNCE_EDIT,
    ANNOUNCE_DELETE

}