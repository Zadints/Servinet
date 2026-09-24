package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class RoleModel {



    public static void addRoleDatabase(Role rol, Set<Permission> perms) {

        Connection conn = LoadDb.getConnection();

        String sqlExists = """
        SELECT 1
        FROM roles
        WHERE name = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sqlExists)) {

            stmt.setString(1, rol.getName());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return;
                }
            }
        }catch (SQLException e) {
            throw new DatabaseException(
                    "Error al guardar el rol en la base de datos", e
            );
        }


        String sqlRole = """
        
            INSERT INTO roles (uuid, name, hexColor)
                VALUES (?, ?, ?);
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sqlRole)) {

            stmt.setString(1, rol.getUuid());
            stmt.setString(2, rol.getName());
            stmt.setString(3, rol.getHexColor());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al guardar el rol en la base de datos", e
            );
        }

        String sqlPermission = """
        INSERT INTO role_permissions (role_uuid, permission)
        VALUES (?, ?);
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sqlPermission)) {

            for (Permission permission : perms) {

                stmt.setString(1, rol.getUuid());
                stmt.setString(2, permission.name());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al guardar los permisos del rol en la base de datos", e
            );
        }
    }

    public static List<Role> getRolesDatabase() {

        String sql = """
        SELECT
            r.uuid,
            r.name,
            r.hexColor,
            rp.permission
        FROM roles r
        LEFT JOIN role_permissions rp
            ON r.uuid = rp.role_uuid
        """;

        Connection conn = LoadDb.getConnection();

        Map<String, Role> roles = new LinkedHashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String uuid = rs.getString("uuid");
                String name = rs.getString("name");
                String color = rs.getString("hexColor");

                Role rol = roles.computeIfAbsent(
                        uuid,
                        id -> new Role(
                                uuid,
                                new HashSet<>(),
                                color,
                                name
                        )
                );

                String permission = rs.getString("permission");

                if (permission != null) {
                    rol.getPermissions().add(
                            Permission.valueOf(permission)
                    );
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener los roles de la base de datos",
                    e
            );
        }

        return new ArrayList<>(roles.values());
    }
}