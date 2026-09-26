package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Role;

import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.DatabaseException;

import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;

import java.util.*;

public class RoleModel {



    public static void addRoleDatabase(Role rol, Set<Permission> perms) {

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No se pudo establecer conexión con la base de datos."
            );
        }

        String sqlExists = """
        SELECT 1
        FROM roles
        WHERE name = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sqlExists)) {

            stmt.setString(1, rol.getName());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    throw new DatabaseException(
                            "Ya existe un rol con ese nombre."
                    );
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

    public static void updateRoleDatabase(
            Role role,
            Set<Permission> permissions
    )
    {

        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No se pudo establecer conexión con la base de datos."
            );
        }

        String updateRoleSql = """
        UPDATE roles
        SET name = ?, hexColor = ?
        WHERE uuid = ?
    """;

        String deletePermissionsSql = """
        DELETE FROM role_permissions
        WHERE role_uuid = ?
    """;

        String insertPermissionSql = """
        INSERT INTO role_permissions (role_uuid, permission)
        VALUES (?, ?)
    """;

        try {

            /*
             * Usamos transacción porque estamos actualizando:
             *
             * 1. Datos del rol
             * 2. Permisos del rol
             *
             * Si algo falla, no queremos dejar el rol a medias.
             */
            conn.setAutoCommit(false);


        /* =========================
           ACTUALIZAR ROL
           ========================= */

            try (PreparedStatement stmt =
                         conn.prepareStatement(updateRoleSql)) {

                stmt.setString(1, role.getName());
                stmt.setString(2, role.getHexColor());
                stmt.setString(3, role.getUuid());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException(
                            "No se encontró el rol que se intenta actualizar."
                    );
                }
            }


        /* =========================
           ELIMINAR PERMISOS ANTERIORES
           ========================= */

            try (PreparedStatement stmt =
                         conn.prepareStatement(deletePermissionsSql)) {

                stmt.setString(1, role.getUuid());

                stmt.executeUpdate();
            }


        /* =========================
           GUARDAR NUEVOS PERMISOS
           ========================= */

            try (PreparedStatement stmt =
                         conn.prepareStatement(insertPermissionSql)) {

                for (Permission permission : permissions) {

                    stmt.setString(
                            1,
                            role.getUuid()
                    );

                    stmt.setString(
                            2,
                            permission.name()
                    );

                    stmt.addBatch();
                }

                stmt.executeBatch();
            }


            conn.commit();

        } catch (SQLException e) {

            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }

            throw new DatabaseException(
                    "No se pudo actualizar el rol",
                    e
            );

        } finally {

            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteRoleDatabase(String roleUuid) {
        Connection conn = LoadDb.getConnection();

        if (conn == null) {
            throw new DatabaseException(
                    "No se pudo establecer conexión con la base de datos."
            );
        }

        try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM role_permissions WHERE role_uuid = ?");
             PreparedStatement s2 = conn.prepareStatement("DELETE FROM roles WHERE uuid = ?")) {
            s1.setString(1, roleUuid);
            s1.executeUpdate();
            s2.setString(1, roleUuid);
            s2.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el rol", e);
        }
    }
}