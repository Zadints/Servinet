package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.dto.database.AuthLogin;
import org.example.servinet.application.port.out.sql.SqlStaffRepository;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqliteStaffAuth implements SqlStaffRepository {

    private static final long SESSION_DAYS = 2;

    private static final String STAFF_COLUMNS =
            "uuid, phone, email, role AS role_id, create_at, display, perfil_img";

    private final DatabaseFactory databaseFactory;

    public SqliteStaffAuth(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    private record StaffRow(
            String uuid,
            String phone,
            String email,
            String roleId,
            LocalDateTime createAt,
            String display,
            byte[] perfilImg
    ) {
        Staff toStaff(Role role) {
            return new Staff(uuid, phone, email, role, createAt, display, perfilImg);
        }
    }

    private StaffRow mapRow(ResultSet rs) throws SQLException {
        return new StaffRow(
                rs.getString("uuid"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("role_id"),
                SqliteUtil.getDateTime(rs, "create_at"),
                rs.getString("display"),
                rs.getBytes("perfil_img")
        );
    }

    public String getStaffActiveHardwareId(String hardwareId) {

        String sql = "SELECT user_uuid, expires_at FROM users_session WHERE hardware_id = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hardwareId);

            String userUuid;
            LocalDateTime expiresAt;

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                userUuid = rs.getString("user_uuid");
                expiresAt = SqliteUtil.getDateTime(rs, "expires_at");
            }

            if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
                SqliteUtil.update(
                        conn,
                        "DELETE FROM users_session WHERE hardware_id = ?",
                        hardwareId
                );
                return null;
            }

            return userUuid;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
            throw new DatabaseException(
                    "Error al consultar la sesión del hardware", e
            );
        }
    }

    public Staff getStaffAndNewSession(String hardwareId, String staffUuid) {

        String sql = "SELECT " + STAFF_COLUMNS + " FROM users WHERE uuid = ?";

        try (Connection connection = databaseFactory.getConnection()) {

            StaffRow row = SqliteUtil.inTransaction(connection, c -> {

                StaffRow found;

                try (PreparedStatement stmt = c.prepareStatement(sql)) {
                    stmt.setString(1, staffUuid);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            return null;
                        }
                        found = mapRow(rs);
                    }
                }

                SqliteUtil.update(
                        c,
                        "INSERT INTO users_session (hardware_id, user_uuid, expires_at) VALUES (?, ?, ?) "
                                + "ON CONFLICT(hardware_id) DO UPDATE SET "
                                + "user_uuid = excluded.user_uuid, expires_at = excluded.expires_at",
                        hardwareId,
                        staffUuid,
                        SqliteUtil.toDb(LocalDateTime.now().plusDays(SESSION_DAYS))
                );

                return found;
            });

            if (row == null) {
                return null;
            }

            return row.toStaff(getStaffRole(row.roleId()));

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al consultar la sesión del hardware", e
            );
        }
    }

    public Staff getStaffByUuid(String staffUuid) {
        String sql = "SELECT " + STAFF_COLUMNS + " FROM users WHERE uuid = ?";

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, staffUuid);

            StaffRow row;

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                row = mapRow(rs);
            }

            return row.toStaff(getStaffRole(row.roleId()));

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener el usuario con UUID: " + staffUuid,
                    e
            );
        }
    }

    public Role getStaffRole(String roleUuid) {
        String roleSql = "SELECT uuid, name, hexColor FROM roles WHERE uuid = ?";
        String permissionSql = "SELECT permission FROM role_permissions WHERE role_uuid = ?";

        try (Connection connection = databaseFactory.getConnection()) {

            Set<Permission> permissions = new HashSet<>();
            Role role;

            try (PreparedStatement stmt = connection.prepareStatement(roleSql)) {
                stmt.setString(1, roleUuid);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    role = new Role(
                            rs.getString("uuid"),
                            permissions,
                            rs.getString("hexColor"),
                            rs.getString("name")
                    );
                }
            }

            try (PreparedStatement stmt = connection.prepareStatement(permissionSql)) {
                stmt.setString(1, roleUuid);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        permissions.add(
                                Permission.valueOf(rs.getString("permission"))
                        );
                    }
                }
            }

            return role;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener el rol " + roleUuid,
                    e
            );
        }
    }

    public AuthLogin getStaffAuthLoginByEmail(String email) {
        String sql = "SELECT password_hash, uuid FROM users WHERE email = ?";

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthLogin(
                            rs.getString("password_hash"),
                            rs.getString("uuid")
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener el usuario con el correo " + email,
                    e
            );
        }
    }

    public void removeStaffSession(String hardwareId) {
        String sql = "DELETE FROM users_session WHERE hardware_id = ?";

        try (Connection connection = databaseFactory.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, hardwareId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al eliminar la sesión del usuario",
                    e
            );
        }
    }

    public void setStaffDatabase(Staff staff, String password) {
        String sql = "INSERT INTO users (uuid, display, email, phone, role, password_hash, create_at, perfil_img) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(connection, c -> {

                if (SqliteUtil.exists(c, "SELECT 1 FROM users WHERE email = ?", staff.getEmail())) {
                    throw new DatabaseException("No register user");
                }

                try (PreparedStatement stmt = c.prepareStatement(sql)) {
                    stmt.setString(1, String.valueOf(staff.getUuid()));
                    stmt.setString(2, staff.getDisplay());
                    stmt.setString(3, staff.getEmail());
                    stmt.setString(4, staff.getPhone());
                    stmt.setString(5, String.valueOf(staff.getRol().getUuid()));
                    stmt.setString(6, password);
                    stmt.setString(7, SqliteUtil.toDb(staff.getCreateAt()));
                    SqliteUtil.setNullableBytes(stmt, 8, staff.getPerfilImg());
                    stmt.executeUpdate();
                }
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al guardar el usuario en la base de datos",
                    e
            );
        }
    }

    public List<Staff> getAllStaff() {

        String sql = "SELECT " + STAFF_COLUMNS + " FROM users ORDER BY create_at";
        List<StaffRow> rows = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rows.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
            throw new DatabaseException(
                    "Error al consultar los usuarios",
                    e
            );
        }

        Map<String, Role> roles = new HashMap<>();
        List<Staff> staffList = new ArrayList<>();

        for (StaffRow row : rows) {
            Role role = roles.computeIfAbsent(
                    row.roleId(),
                    id -> id == null ? null : getStaffRole(id)
            );
            staffList.add(row.toStaff(role));
        }

        return staffList;
    }

    public boolean isNameOrEmailTaken(String email) {

        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo comprobar el usuario",
                    e
            );
        }
    }

    public void updateStaff(
            String uuid,
            String email,
            String roleUuid,
            String newPasswordHash,
            byte[] imageBytes
    ) {
        String sql = "UPDATE users SET email = COALESCE(?, email), role = COALESCE(?, role), "
                + "password_hash = COALESCE(?, password_hash), perfil_img = COALESCE(?, perfil_img) "
                + "WHERE uuid = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            SqliteUtil.setNullableString(stmt, 1, email);
            SqliteUtil.setNullableString(stmt, 2, roleUuid);
            SqliteUtil.setNullableString(stmt, 3, newPasswordHash);
            SqliteUtil.setNullableBytes(stmt, 4, imageBytes);
            stmt.setString(5, uuid);

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException(
                        "No se encontró el usuario que se desea actualizar"
                );
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el usuario",
                    e
            );
        }
    }

    public void deleteStaff(String uuid) {

        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {
                SqliteUtil.update(c, "DELETE FROM users_session WHERE user_uuid = ?", uuid);

                if (SqliteUtil.update(c, "DELETE FROM users WHERE uuid = ?", uuid) == 0) {
                    throw new DatabaseException("No se encontró el usuario que se desea eliminar");
                }
            });

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el usuario", e);
        }
    }

    public int countStaffWithRole(String roleUuid) {
        String sql = "SELECT COUNT(*) AS total FROM users WHERE role = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = executeCountQuery(stmt, roleUuid)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

            return 0;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo contar los usuarios del rol", e
            );
        }
    }

    private ResultSet executeCountQuery(
            PreparedStatement stmt,
            String roleUuid
    ) throws SQLException {
        stmt.setString(1, roleUuid);
        return stmt.executeQuery();
    }

    public void updatePassword(String uuid, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE uuid = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setString(2, uuid);

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException(
                        "No se encontró el usuario cuya contraseña se desea actualizar"
                );
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo cambiar la contraseña", e
            );
        }
    }

}
