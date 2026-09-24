package org.example.servinet.core.domain.enums;

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
    APP_RENAME
}