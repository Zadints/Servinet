package org.example.servinet.core.domain.enums;

public enum Permission {

    BYPASS,

    /*
     * Permisos para ver botones en el sidebar
     *  o hacer clic en ellos
     */
    BTN_SIDEBAR_DASHBOARD_VIEW,
    BTN_SIDEBAR_ADMINISTRATION_VIEW,
    BTN_SIDEBAR_ANNOUNCE_VIEW,
    BTN_SIDEBAR_ANTENNAS_VIEW,
    BTN_SIDEBAR_BACKUPS_VIEW,
    BTN_SIDEBAR_CLIENTS_VIEW,
    BTN_SIDEBAR_SALES_VIEW,
    BTN_SIDEBAR_TECHNICAL_VIEW,


    /*
     * Permisos para todo lo respecto al
     * dashbaord
     */
    DASH_BYPASS,
        DASH_VIEW_TOPS,
        DASH_VIEW_GRAPH,
    /*------------------------------------------------------------------------------
     * Permisos para la opción de administración
     * de la aplicación
     */

    AD_BYPASS,

    AD_PERSONAL_SECTION, //¡Bloque 1!
        AD_SEARCH_PERSONAL,
        AD_CREATE_PERSONAL,
        AD_VIEW_PERSONAL,
        AD_EDIT_PERSONAL,
        AD_DELETE_PERSONAL,
            /*
             * Este sector ds para ver estadisticas
             * del personal y administrar su sueldo
             */
            AD_VIEW_COMMISSIONS_PERSONAL,
            AD_EDIT_COMMISSIONS_PERSONAL,
            AD_DELETE_COMMISSIONS_PERSONAL,

    AD_ACTIVITY_PERSONAL_SECTION, //¡Bloque 2!
        AD_ACTIVITY_PERSONAL,
        AD_VIEW_LOG_PERSONAL,

    AD_OWNER_OPTIONS_SECTION,//¡Bloque 3!
        APP_CHANGE_NAME,
        APP_CHANGE_LOGO,
        APP_SECURITY_CONFIG,
        APP_GENERAL_CONFIG,
        APP_CONF_SECTOR_ANT,
        APP_CONF_ROL_PERMS,

    /*------------------------------------------------------------------------------
     * Permisos para la opción de anuncios de la app
     * de la aplicación
     */
    ANNOUNCE_CREATE,
    ANNOUNCE_VIEW,
    ANNOUNCE_DELETE,
    ANNOUNCE_EDIT,

    ANT_BYPASS,
        ANT_CREATE,
        ANT_DELETE,
        ANT_EDIT,
        ANT_VIEW_INFO,
        ANT_VIEW_ANTENNAS,
        ANT_START_MAINTE,
        ANT_END_MAINT,
        ANT_GO_ACTIVE,
        ANT_GO_DESACTIVE,

    BACKUPS_BYPASS,

    CLIENTS_BYPASS,
        CLIENT_DELETE,
        CLIENT_PAY_VIEW,
        CLIENT_INFO,
        CLIENT_SEARCH,

    SELL_BYPASS,
        SELL_CREATE_CONTRATE,
        SELL_RENEW_CONTRATE,
        SELL_DELETE_CONTRATE,


    TECHNICIAN_BYPASS,

    MY_PROFILE_INFO
}
