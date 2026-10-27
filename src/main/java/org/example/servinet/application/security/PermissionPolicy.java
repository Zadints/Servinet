
package org.example.servinet.application.security;

import org.example.servinet.domain.enums.Permission;

import org.example.servinet.domain.enums.ModuleApp;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Política centralizada de permisos de ServiNet.
 *
 * Evalúa los permisos asignados al rol.
 * Nunca compara nombres como Dueño, Técnico o Vendedor.
 */
public final class PermissionPolicy {

    private final Map<Permission, EnumSet<Permission>>
            GROUPS = createGroups();


    private final EnumSet<Permission> NON_FUNCTIONAL =
            EnumSet.of(
                    Permission.AD_PERSONAL_SECTION,
                    Permission.AD_ACTIVITY_PERSONAL_SECTION,
                    Permission.AD_OWNER_OPTIONS_SECTION,
                    Permission.APP_CONF_SECTOR_ANT
            );

    private final EnumSet<Permission> effectivePermissions;

    private final EnumSet<ModuleApp> enabledModules =
            EnumSet.noneOf(ModuleApp.class);


    public void setAssignedPolicy(Set<Permission> assigned){
        if (assigned != null) {
            effectivePermissions.addAll(assigned);
        }

        if (effectivePermissions.contains(Permission.BYPASS)) {

            effectivePermissions.addAll(
                    EnumSet.allOf(Permission.class)
            );

            enabledModules.addAll(
                    EnumSet.allOf(ModuleApp.class)
            );

            return;
        }


        for (Map.Entry<Permission, EnumSet<Permission>>
                entry : GROUPS.entrySet()) {

            if (effectivePermissions.contains(entry.getKey())) {

                effectivePermissions.addAll(entry.getValue());
            }
        }


        for (Permission permission : effectivePermissions) {

            if (NON_FUNCTIONAL.contains(permission)) {
                continue;
            }

            ModuleApp module = moduleFor(permission);

            if (module != null) {
                enabledModules.add(module);
            }
        }
    }

    public PermissionPolicy() {
        effectivePermissions = EnumSet.noneOf(Permission.class);
    }


    public boolean allows(Permission permission) {

        return permission != null
                && effectivePermissions.contains(permission);
    }

    public boolean allowsAny(Permission... permissions) {

        for (Permission permission : permissions) {

            if (allows(permission)) {
                return true;
            }
        }

        return false;
    }

    public boolean canOpen(ModuleApp module) {

        return module != null
                && enabledModules.contains(module);
    }

    private Map<Permission, EnumSet<Permission>>
    createGroups() {

        Map<Permission, EnumSet<Permission>> groups =
                new EnumMap<>(Permission.class);

        groups.put(
                Permission.DASH_BYPASS,
                matching("DASH_")
        );

        groups.put(
                Permission.AD_BYPASS,
                matching("AD_", "APP_")
        );

        groups.put(
                Permission.ANT_BYPASS,
                matching("ANT_")
        );

        groups.put(
                Permission.CLIENTS_BYPASS,
                matching("CLIENT")
        );

        groups.put(
                Permission.SELL_BYPASS,
                matching("SELL_")
        );

        groups.put(
                Permission.TECHNICIAN_BYPASS,
                matching("TECHNICIAN_")
        );

        return groups;
    }

    private EnumSet<Permission> matching(
            String... prefixes) {

        EnumSet<Permission> result =
                EnumSet.noneOf(Permission.class);

        for (Permission permission : Permission.values()) {

            for (String prefix : prefixes) {

                if (permission.name().startsWith(prefix)) {

                    result.add(permission);
                    break;
                }
            }
        }

        return result;
    }

    private ModuleApp moduleFor(Permission permission) {

        String name = permission.name();

        if (name.startsWith("DASH_")) {
            return ModuleApp.DASHBOARD;
        }

        if (name.startsWith("AD_")
                || name.startsWith("APP_")) {
            return ModuleApp.ADMINISTRATION;
        }

        if (name.startsWith("ANNOUNCE_")) {
            return ModuleApp.ANNOUNCEMENTS;
        }

        if (name.startsWith("ANT_")) {
            return ModuleApp.ANTENNAS;
        }

        if (name.startsWith("BACKUPS_")) {
            return ModuleApp.BACKUPS;
        }

        if (name.startsWith("CLIENT")) {
            return ModuleApp.CLIENTS;
        }

        if (name.startsWith("SELL_")) {
            return ModuleApp.SALES;
        }

        if (name.startsWith("TECHNICIAN_")) {
            return ModuleApp.TECHNICIANS;
        }

        // BTN_SIDEBAR_* no da acceso sin permisos internos.
        return null;
    }
}

