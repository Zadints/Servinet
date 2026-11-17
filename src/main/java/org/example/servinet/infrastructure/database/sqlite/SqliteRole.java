package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.port.out.sql.SqlLimitedDataRepository;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SqliteRole implements SqlLimitedDataRepository<Role, List<Role>> {

    private final DatabaseFactory databaseFactory;

    public SqliteRole(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void add(Role rol) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {
                SqliteUtil.update(
                        c,
                        "INSERT INTO roles (uuid, name, hexColor) VALUES (?, ?, ?)",
                        rol.getUuid(), rol.getName(), rol.getHexColor()
                );
                insertPermissions(c, rol);
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al crear el rol porque el rol ya existe", e
            );
        }
    }

    @Override
    public Role get(String id) {
        return null;
    }

    @Override
    public List<Role> getAll() {
        String sql = "SELECT r.uuid, r.name, r.hexColor, rp.permission "
                + "FROM roles r LEFT JOIN role_permissions rp ON r.uuid = rp.role_uuid";

        try {

            Map<String, Role> roles = new LinkedHashMap<>();

            try (Connection conn = databaseFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    String uuid = rs.getString("uuid");

                    Role role = roles.get(uuid);

                    if (role == null) {

                        role = new Role(
                                uuid,
                                new HashSet<>(),
                                rs.getString("hexColor"),
                                rs.getString("name")
                        );

                        roles.put(uuid, role);
                    }

                    String permissionName = rs.getString("permission");

                    if (permissionName != null) {
                        role.getPermissions().add(
                                Permission.valueOf(permissionName)
                        );
                    }
                }
            }
            return new ArrayList<>(roles.values());
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al obtener los roles de la base de datos.",
                    e
            );
        }
    }

    @Override
    public void delete(String id) {
        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {
                SqliteUtil.update(c, "DELETE FROM role_permissions WHERE role_uuid = ?", id);
                SqliteUtil.update(c, "DELETE FROM roles WHERE uuid = ?", id);
            });

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el rol", e);
        }
    }

    @Override
    public void update(Role role) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {
                int changed = SqliteUtil.update(
                        c,
                        "UPDATE roles SET name = ?, hexColor = ? WHERE uuid = ?",
                        role.getName(), role.getHexColor(), role.getUuid()
                );

                if (changed == 0) {
                    throw new DatabaseException("No se encontró el rol que se intenta actualizar.");
                }

                SqliteUtil.update(c, "DELETE FROM role_permissions WHERE role_uuid = ?", role.getUuid());
                insertPermissions(c, role);
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el rol",
                    e
            );
        }
    }

    private void insertPermissions(Connection conn, Role role) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO role_permissions (role_uuid, permission) VALUES (?, ?)")) {

            for (Permission permission : role.getPermissions()) {
                stmt.setString(1, role.getUuid());
                stmt.setString(2, permission.name());
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }
}
