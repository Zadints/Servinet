package org.example.servinet.core.application.service;

import org.example.servinet.core.domain.enums.Permission;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.example.servinet.core.domain.enums.Permission.*;

public class RoleTemplates {

    public static Map<String, Set<Permission>> all() {
        Map<String, Set<Permission>> templates = new LinkedHashMap<>();


        templates.put("Administrador", EnumSet.complementOf(EnumSet.of(BYPASS)));

        templates.put("Técnico", EnumSet.of(
                DASH_VIEW, ANOUN_VIEW,
                ANT_VIEW_ANTENNAS, ANT_VIEW_INFO, ANT_EDIT,
                ANT_START_MAINTE, ANT_END_MAINT, ANT_GO_ACTIVE, ANT_GO_DESACTIVE));

        templates.put("Vendedor", EnumSet.of(
                DASH_VIEW, ANOUN_VIEW, SELL,
                CLIENT_SEARCH, CLIENT_INFO, CLIENT_PAY_VIEW));

        templates.put("Solo lectura", EnumSet.of(
                DASH_VIEW, ANOUN_VIEW,
                ANT_VIEW_ANTENNAS, ANT_VIEW_INFO, CLIENT_INFO));

        return templates;
    }
}
