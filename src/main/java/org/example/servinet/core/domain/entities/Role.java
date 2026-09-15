package org.example.servinet.core.domain.entities;

import org.example.servinet.core.domain.enums.Permission;
import java.util.Set;
import java.util.UUID;

public class Role {
    private final String uuid;
    private String name;
    private String hexColor;
    private Set<Permission> permissions;

    public Role(String uuid, Set<Permission> permissions, String hexColor, String name) {
        this.uuid = uuid;
        this.permissions = permissions;
        this.hexColor = hexColor;
        this.name = name;
    }



    public Set<Permission> getPermissions() {
        return permissions;
    }

    public String getName() {
        return name;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getUuid() {
        return uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public void removePermission(Permission e){
        if (!hasPermission(e))
            return;
        this.permissions.remove(e);
    }
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
    public boolean addPermissions(Permission e) {

        if (!hasPermission(e))
            return false;

        this.permissions.add(e);
        return true;
    }
}
