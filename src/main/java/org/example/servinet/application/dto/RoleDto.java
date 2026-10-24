package org.example.servinet.application.dto;

import org.example.servinet.domain.enums.Permission;
import java.util.Set;

public class RoleDto {
    private String name;
    private String hexColor;
    private Set<Permission> permissions;

    public RoleDto( Set<Permission> permissions, String hexColor, String name) {
        this.permissions = permissions;
        this.hexColor = hexColor;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getHexColor() {
        return hexColor;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
