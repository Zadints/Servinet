package org.example.servinet.domain.enums;

public enum LogType {
    LOGIN("Inicio de sesión"),
    LOGOUT("Cierre de sesión"),
    USER_CREATE("Usuario creado"),
    USER_EDIT("Usuario editado"),
    USER_DELETE("Usuario eliminado"),
    PASSWORD_CHANGE("Contraseña cambiada"),
    ADD_ROLE("Rol agregado"),
    REMOVE_ROLE("Rol eliminado"),
    EDIT_ROLE("Rol editado"),
    ANT_REMOVE("Antena eliminada"),
    ANT_CREATE("Antena creada"),
    PERM_ADD("Permiso agregado"),
    PERM_REMOVE("Permiso retirado"),
    APP_RENAME("Nombre de aplicación cambiado"),
    APP_CHANGUE_IMAGE("Imagen de aplicación cambiada"),
    HOME_INSTALLED("Instalación completada"),
    ORDER_ACCEPTED("Orden aceptada"),
    ORDER_CANCELLED("Orden cancelada"),
    ANNOUNCE_CREATE("Anuncio creado"),
    ANNOUNCE_EDIT("Anuncio editado"),
    ANNOUNCE_DELETE("Anuncio eliminado"),
    SELL("Venta registrada"),
    PAID("Pago registrado");
    private final String displayName;

    LogType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}