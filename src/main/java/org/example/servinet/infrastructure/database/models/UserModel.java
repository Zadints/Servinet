package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.enums.Role;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.config.LoadDb;
import org.example.servinet.core.domain.entities.User;
import java.sql.*;
import java.time.LocalDateTime;

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
        INSERT INTO users_session (
            hardware_id,user_uuid
        )
        VALUES (?, ?);
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hardwareId);
            stmt.setString(2, userUuid);

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

                    User user = new User(
                            rs.getString("uuid"),
                            rs.getString("email"),
                            Role.valueOf(rs.getString("rol")),
                            rs.getTimestamp("create_at").toLocalDateTime(),
                            rs.getString("password_hash"),
                            rs.getString("display"),
                            rs.getString("perfil_img")
                    );

                    return user;
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener el usuario" + uuid , e);
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

                    User user = new User(
                          rs.getString("uuid"),
                            rs.getString("email"),
                            Role.valueOf(rs.getString("rol")),
                            rs.getTimestamp("create_at").toLocalDateTime(),
                            rs.getString("password_hash"),
                            rs.getString("display"),
                            rs.getString("perfil_img")
                    );

                    return user;
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo obtener el usuario" + name, e );
        }

        return null;
    }

    public static void setUserDatabase(User user) {

        String sql = """
        INSERT INTO users (
            uuid,
            display,
            email,
            rol,
            password_hash,
            create_at,
            perfil_img
        )
        VALUES (?, ?, ?, ?, ?, ?, ?);
        """;

        Connection conn = LoadDb.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUuid());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRol().name());
            stmt.setString(5, user.getPasswordHash());
            stmt.setTimestamp(6, Timestamp.valueOf(user.getCreateAt()));
            stmt.setString(7, user.getPerfilImg());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error al guardar el usuario en la base de datos", e
            );
        }
    }
}