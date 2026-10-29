package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.dto.database.AuthLogin;
import org.example.servinet.application.port.out.StaffRepository;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.infrastructure.database.DatabaseFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class SqlStaffAuthRepository implements StaffRepository {

    private final DatabaseFactory databaseFactory;

    public SqlStaffAuthRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    /**
     * Clases dedicadas al rregistro de usuarios
     **/

    public String getStaffActiveHardwareId(String hardwareId) {

        String sql = "{call dbo.sp_getStaffIsActiveHadwareId(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, hardwareId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                LocalDateTime expiresAt =
                        rs.getTimestamp("expires_at").toLocalDateTime();

                if (expiresAt.isBefore(LocalDateTime.now())) {

                    String removeSql = "{call dbo.sp_removeStaffActiveHadwareId(?)}";

                    try (CallableStatement deleteStmt = conn.prepareCall(removeSql)) {
                        deleteStmt.setString(1, hardwareId);
                        deleteStmt.execute();
                    }

                    return null;
                }

                return rs.getString("user_uuid");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
            throw new DatabaseException(
                    "Error al consultar la sesión del hardware", e
            );
        }
    }

    public Staff getStaffAndNewSession(String hardwareId, String staffUuid) {

        String sql = "{call dbo.sp_getStaffAndNewSession(?, ?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {

            stmt.setString(1, hardwareId);
            stmt.setString(2, staffUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    Role rol = getStaffRole(rs.getString("role_id"));

                    return new Staff(
                            rs.getString("uuid"),
                            rs.getString("email"),
                            rol,
                            rs.getTimestamp("create_at").toLocalDateTime(),
                            rs.getString("display"),
                            rs.getBytes("perfil_img")
                    );
                }

                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al consultar la sesión del hardware", e
            );
        }
    }

    public Staff getStaffByUuid(String staffUuid) {
        String sql = "{call dbo.sp_getStaffByUuid(?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {

            stmt.setString(1, staffUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Role role = getStaffRole(rs.getString("role_id"));

                return new Staff(
                        rs.getString("uuid"),
                        rs.getString("email"),
                        role,
                        rs.getTimestamp("create_at").toLocalDateTime(),
                        rs.getString("display"),
                        rs.getBytes("perfil_img")
                );
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo obtener el usuario con UUID: " + staffUuid,
                    e
            );
        }
    }
    public Role getStaffRole(String roleUuid) {
        String sql = "{call dbo.sp_getRole(?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {

            stmt.setObject(1, roleUuid);

            if (!stmt.execute()) {
                return null;
            }

            Set<Permission> permissions = new HashSet<>();
            Role role;

            try (ResultSet rs = stmt.getResultSet()) {
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


            if (stmt.getMoreResults()) {
                try (ResultSet rs = stmt.getResultSet()) {
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
        String sql = "{call dbo.sp_getUserPasswordByEmail(?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {

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
        String sql = "{call dbo.sp_removeStaffActiveHadwareId(?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {

            stmt.setString(1, hardwareId);
            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al eliminar la sesión del usuario",
                    e
            );
        }
    }

    public void setStaffDatabase(Staff staff, String password) {
        String sql = "{call dbo.sp_createUser(?,?,?,?,?,?,?)}";

        try (Connection connection = databaseFactory.getConnection();
             CallableStatement stmt = connection.prepareCall(sql)) {

            stmt.setObject(1, staff.getUuid());
            stmt.setString(2, staff.getDisplay());
            stmt.setString(3, staff.getEmail());
            stmt.setObject(4, staff.getRol().getUuid());
            stmt.setString(5, password);
            stmt.setTimestamp(6, Timestamp.valueOf(staff.getCreateAt()));
            stmt.setBytes(7, staff.getPerfilImg());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new DatabaseException("No register user");
                }
            }



        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al guardar el usuario en la base de datos",
                    e
            );
        }
    }



    public List<Staff> getAllStaff() {

        String sql = "{call dbo.sp_getAllStaff()}";
        List<Staff> staffList = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Role role = getStaffRole(rs.getString("role_id"));

                Staff staff = new Staff(
                        rs.getString("uuid"),
                        rs.getString("email"),
                        role,
                        rs.getTimestamp("create_at").toLocalDateTime(),
                        rs.getString("display"),
                        rs.getBytes("perfil_img")
                );

                staffList.add(staff);
            }

            return staffList;

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al consultar los usuarios",
                    e
            );
        }
    }


    public boolean isNameOrEmailTaken(String name, String email) {

        String sql = "{call dbo.sp_isNameOrEmail(?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);

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
            String name,
            String email,
            String roleUuid,
            String newPasswordHash,
            byte[] imageBytes
    ) {
        String sql = "{call dbo.sp_updateUser(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, uuid);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, roleUuid);

            if (newPasswordHash != null) {
                stmt.setString(5, newPasswordHash);
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            if (imageBytes != null) {
                stmt.setBytes(6, imageBytes);
            } else {
                stmt.setNull(6, Types.VARBINARY);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("affected_rows") == 0) {
                    throw new DatabaseException(
                            "No se encontró el usuario que se desea actualizar"
                    );
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el usuario",
                    e
            );
        }
    }

    public void deleteStaff(String uuid) {
        String sql = "{call dbo.sp_deleteUser(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, uuid);
            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el usuario", e);
        }
    }

    public int countStaffWithRole(String roleUuid) {
        String sql = "{call dbo.sp_countUsersWithRole(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
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
            CallableStatement stmt,
            String roleUuid
    ) throws SQLException {
        stmt.setString(1, roleUuid);
        return stmt.executeQuery();
    }

    public void updatePassword(String uuid, String newPasswordHash) {
        String sql = "{call dbo.sp_updatePassword(?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, uuid);
            stmt.setString(2, newPasswordHash);
            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo cambiar la contraseña", e
            );
        }
    }

}