package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.infrastructure.database.config.LoadDb;
import org.example.servinet.core.domain.entities.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserModel {

    public static User getUserDatabaseHadwareId(String hardwareId) {

        String sql = """
        SELECT user_uuid, expires_at FROM users_session WHERE hardware_id = ?
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hardwareId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    System.out.println("si encontrado en db");
                    LocalDateTime expiresAt =
                            rs.getTimestamp("expires_at").toLocalDateTime();

                    if (expiresAt.isBefore(LocalDateTime.now())) {

                        String deleteSql = """
                        DELETE FROM users_session
                        WHERE hardware_id = ?
                        """;

                        try (PreparedStatement deleteStmt =
                                     conn.prepareStatement(deleteSql)) {

                            deleteStmt.setString(1, hardwareId);
                            deleteStmt.executeUpdate();
                        }

                        return null;
                    }

                    return getUserDatabaseUuid(rs.getString("user_uuid"));
                }
            }

        } catch (SQLException e) {
            return null;
        }

        return null;
    }

    public static void setUserDatabaseHadwareId(String hardwareId, String userUuid) {

        String sql = """
       IF NOT EXISTS (
            SELECT 1
            FROM users_session
            WHERE hardware_id = ?
        )
        BEGIN
            INSERT INTO users_session (
                hardware_id, user_uuid
            )
            VALUES (?, ?);
        END
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hardwareId);
            stmt.setString(2, hardwareId);
            stmt.setString(3, userUuid);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al guardar el usuario en la base de datos", e
            );
        }
    }
    public static User getUserDatabaseUuid(String uuid) {

        String sql = """
        SELECT *FROM users WHERE uuid = ?
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    String idRol = rs.getString("role");

                    Role rol = getRolUserDatabase(idRol);

                    User user = new User(
                            rs.getString("uuid"),
                            rs.getString("email"),
                            rol,
                            rs.getTimestamp("create_at").toLocalDateTime(),
                            rs.getString("password_hash"),
                            rs.getString("display"),
                            ImageConverter.toImage(rs.getBytes("perfil_img"))
                    );

                    return user;
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener el usuario" + uuid , e);
        }

        return null;
    }

    public static Set<Permission> getPermissionRolDatabase(String idRol) {

        String sql = """
        SELECT permission
        FROM role_permissions
        WHERE role_uuid = ?
        """;

        Connection conn = LoadDb.getConnection();

        Set<Permission> perms = new HashSet<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idRol);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    String permission = rs.getString("permission");

                    perms.add(Permission.valueOf(permission));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los permisos del rol " + idRol,
                    e
            );
        }

        return perms;
    }

    public static Role getRolUserDatabase(String idRol) {

        String sql = """
        SELECT * FROM roles WHERE uuid = ?
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idRol);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {



                    Role newRol = new Role(
                        rs.getString("uuid"),
                            getPermissionRolDatabase(idRol),
                        rs.getString("hexColor"),
                        rs.getString("name")
                    );
                    return newRol;
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener el rol" + idRol, e );
        }

        return null;
    }
    public static User getUserDatabase(String name) {

        String sql = """
        SELECT *FROM users WHERE display = ?
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    String idRol = rs.getString("role");

                    Role rol = getRolUserDatabase(idRol);

                    User user = new User(
                          rs.getString("uuid"),
                            rs.getString("email"),
                            rol,
                            rs.getTimestamp("create_at").toLocalDateTime(),
                            rs.getString("password_hash"),
                            rs.getString("display"),
                            ImageConverter.toImage(rs.getBytes("perfil_img"))
                    );

                    return user;
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener el usuario" + name, e );
        }

        return null;
    }
    public static void deleteUserSession(String hardwareId) {

        String sql = """
        DELETE FROM users_session
        WHERE hardware_id = ?
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hardwareId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al eliminar la sesión del usuario", e
            );
        }
    }
    public static void setUserDatabase(User user, byte[] imageToSaveDb) {

        Role rol = user.getRol();
        
        String sql = """
        IF NOT EXISTS (
            SELECT 1
            FROM users
            WHERE email = ?
        )
        BEGIN
            INSERT INTO users (
                uuid,
                display,
                email,
                role,
                password_hash,
                create_at,
                perfil_img
            )
            VALUES (?, ?, ?, ?, ?, ?, ?);
        END
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());

            stmt.setString(2, user.getUuid());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, rol.getUuid());
            stmt.setString(6, user.getPasswordHash());
            stmt.setTimestamp(7, Timestamp.valueOf(user.getCreateAt()));
            stmt.setBytes(8, imageToSaveDb);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DatabaseException(
                    "Error al guardar el usuario en la base de datos", e
            );
        }
    }

    // ============ ADMINISTRACIÓN DE USUARIOS ============

    public static List<User> getAllUsers() {

        Map<String, Role> rolesByUuid = new HashMap<>();
        for (Role r : RoleModel.getRolesDatabase()) {
            rolesByUuid.put(r.getUuid(), r);
        }

        String sql = "SELECT * FROM users ORDER BY create_at";
        Connection conn = LoadDb.getConnection();
        List<User> users = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getString("uuid"),
                        rs.getString("email"),
                        rolesByUuid.get(rs.getString("role")),
                        rs.getTimestamp("create_at").toLocalDateTime(),
                        rs.getString("password_hash"),
                        rs.getString("display"),
                        ImageConverter.toImage(rs.getBytes("perfil_img"))
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener la lista de usuarios", e);
        }
        return users;
    }

    public static boolean isNameOrEmailTaken(String name, String email, String exceptUuid) {
        String sql = "SELECT 1 FROM users WHERE (display = ? OR email = ?)";
        if (exceptUuid != null) sql += " AND uuid <> ?";

        Connection conn = LoadDb.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            if (exceptUuid != null) stmt.setString(3, exceptUuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo comprobar el usuario", e);
        }
    }

    public static void updateUser(String uuid, String name, String email, String roleUuid,
                                  String newPasswordHash, byte[] imageBytes) {
        StringBuilder sql = new StringBuilder("UPDATE users SET display = ?, email = ?, role = ?");
        if (newPasswordHash != null) sql.append(", password_hash = ?");
        if (imageBytes != null) sql.append(", perfil_img = ?");
        sql.append(" WHERE uuid = ?");

        Connection conn = LoadDb.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            stmt.setString(i++, name);
            stmt.setString(i++, email);
            stmt.setString(i++, roleUuid);
            if (newPasswordHash != null) stmt.setString(i++, newPasswordHash);
            if (imageBytes != null) stmt.setBytes(i++, imageBytes);
            stmt.setString(i, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo actualizar el usuario", e);
        }
    }

    public static void deleteUser(String uuid) {
        Connection conn = LoadDb.getConnection();
        try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM users_session WHERE user_uuid = ?");
             PreparedStatement s2 = conn.prepareStatement("DELETE FROM users WHERE uuid = ?")) {
            s1.setString(1, uuid);
            s1.executeUpdate();
            s2.setString(1, uuid);
            s2.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el usuario", e);
        }
    }

    public static int countUsersWithRole(String roleUuid) {
        Connection conn = LoadDb.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role = ?")) {
            stmt.setString(1, roleUuid);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo contar los usuarios del rol", e);
        }
    }

    public static void updatePassword(String uuid, String newPasswordHash) {
        Connection conn = LoadDb.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE uuid = ?")) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo cambiar la contraseña", e);
        }
    }

}