
package org.example.servinet.application.security;

import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.Permission;

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

    public enum Module {
        DASHBOARD,
        ADMINISTRATION,
        ANNOUNCEMENTS,
        ANTENNAS,
        BACKUPS,
        CLIENTS,
        SALES,
        TECHNICIANS
    }

    // Los BYPASS de módulos se resuelven por separado.
    private final Map<Permission, EnumSet<Permission>>
            GROUPS = createGroups();

    // Estos permisos no muestran contenido por sí solos.
    private final EnumSet<Permission> NON_FUNCTIONAL =
            EnumSet.of(
                    Permission.AD_PERSONAL_SECTION,
                    Permission.AD_ACTIVITY_PERSONAL_SECTION,
                    Permission.AD_OWNER_OPTIONS_SECTION,
                    Permission.APP_CONF_SECTOR_ANT
            );

    private final EnumSet<Permission> effectivePermissions;

    private final EnumSet<Module> enabledModules =
            EnumSet.noneOf(Module.class);

    // CONSTRUCTOR
    public PermissionPolicy(Set<Permission> assigned) {

        effectivePermissions =
                EnumSet.noneOf(Permission.class);

        if (assigned != null) {
            effectivePermissions.addAll(assigned);
        }

        // El propietario tiene acceso completo.
        if (effectivePermissions.contains(Permission.BYPASS)) {

            effectivePermissions.addAll(
                    EnumSet.allOf(Permission.class)
            );

            enabledModules.addAll(
                    EnumSet.allOf(Module.class)
            );

            return;
        }

        // Resolver los permisos generales una sola vez.
        for (Map.Entry<Permission, EnumSet<Permission>>
                entry : GROUPS.entrySet()) {

            if (effectivePermissions.contains(entry.getKey())) {

                effectivePermissions.addAll(entry.getValue());
            }
        }

        // Identificar los módulos con contenido permitido.
        for (Permission permission : effectivePermissions) {

            if (NON_FUNCTIONAL.contains(permission)) {
                continue;
            }

            Module module = moduleFor(permission);

            if (module != null) {
                enabledModules.add(module);
            }
        }
    }

    // VERIFICAR UN PERMISO
    public boolean allows(Permission permission) {

        return permission != null
                && effectivePermissions.contains(permission);
    }

    // VERIFICAR SI TIENE AL MENOS UNO
    public boolean allowsAny(Permission... permissions) {

        for (Permission permission : permissions) {

            if (allows(permission)) {
                return true;
            }
        }

        return false;
    }

    // VERIFICAR ACCESO A UN MÓDULO
    public boolean canOpen(Module module) {

        return module != null
                && enabledModules.contains(module);
    }

    // CONFIGURACIÓN DE BYPASS POR MÓDULO
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

    // OBTENER PERMISOS SEGÚN SUS PREFIJOS
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

    // IDENTIFICAR A QUÉ MÓDULO PERTENECE CADA PERMISO
    private Module moduleFor(Permission permission) {

        String name = permission.name();

        if (name.startsWith("DASH_")) {
            return Module.DASHBOARD;
        }

        if (name.startsWith("AD_")
                || name.startsWith("APP_")) {
            return Module.ADMINISTRATION;
        }

        if (name.startsWith("ANNOUNCE_")) {
            return Module.ANNOUNCEMENTS;
        }

        if (name.startsWith("ANT_")) {
            return Module.ANTENNAS;
        }

        if (name.startsWith("BACKUPS_")) {
            return Module.BACKUPS;
        }

        if (name.startsWith("CLIENT")) {
            return Module.CLIENTS;
        }

        if (name.startsWith("SELL_")) {
            return Module.SALES;
        }

        if (name.startsWith("TECHNICIAN_")) {
            return Module.TECHNICIANS;
        }

        // BTN_SIDEBAR_* no da acceso sin permisos internos.
        return null;
    }
}

