package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Role;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.infrastructure.database.config.LoadDb;
import org.example.servinet.core.domain.entities.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
}