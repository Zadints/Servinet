package org.example.servinet.infrastructure.database.models;

import org.example.servinet.core.domain.entities.Announcement;
import org.example.servinet.core.domain.entities.AnnouncementComment;
import org.example.servinet.core.domain.exception.DatabaseException;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.infrastructure.database.config.LoadDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementModel {

    private static Connection connection() {
        Connection conn = LoadDb.getConnection();
        if (conn == null) {
            throw new DatabaseException("No se pudo establecer conexión con la base de datos.");
        }
        return conn;
    }

    public static void insert(Announcement a, byte[] imageBytes) {

        String sql = """
        INSERT INTO announce (uuid, priority, title, description, author, author_uuid, image, sendAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Connection conn = connection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.getUuid());
            stmt.setString(2, a.getPriority());
            stmt.setString(3, a.getTitle());
            stmt.setString(4, a.getDescription());
            stmt.setString(5, a.getAuthorName());
            stmt.setString(6, a.getAuthorUuid());
            stmt.setBytes(7, imageBytes);
            stmt.setTimestamp(8, Timestamp.valueOf(a.getSendAt()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo guardar el anuncio", e);
        }
    }

    public static void update(String uuid, String title, String description, String priority,
                              byte[] newImageBytes, boolean replaceImage) {

        String sql = """
        UPDATE announce
        SET title = ?,
            description = ?,
            priority = ?,
            image = CASE WHEN ? = 1 THEN ? ELSE image END,
            update_at = SYSDATETIME()
        WHERE uuid = ?
        """;

        Connection conn = connection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, priority);
            stmt.setInt(4, replaceImage ? 1 : 0);
            stmt.setBytes(5, newImageBytes);
            stmt.setString(6, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo actualizar el anuncio " + uuid, e);
        }
    }

    public static void delete(String uuid) {

        String sql = "DELETE FROM announce WHERE uuid = ?";

        Connection conn = connection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el anuncio " + uuid, e);
        }
    }

    public static List<Announcement> getAll(String userUuid) {

        String sql = """
        SELECT
            a.uuid, a.priority, a.title, a.description, a.author, a.author_uuid,
            a.image, a.sendAt, a.update_at,
            (SELECT COUNT(*) FROM announce_likes l WHERE l.announce_uuid = a.uuid) AS like_count,
            (SELECT COUNT(*) FROM announce_comments c WHERE c.announce_uuid = a.uuid) AS comment_count,
            CASE WHEN EXISTS (
                SELECT 1 FROM announce_likes l2
                WHERE l2.announce_uuid = a.uuid AND l2.user_uuid = ?
            ) THEN 1 ELSE 0 END AS liked_by_me
        FROM announce a
        ORDER BY a.sendAt DESC
        """;

        Connection conn = connection();
        List<Announcement> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    byte[] imageBytes = rs.getBytes("image");
                    Timestamp updateAt = rs.getTimestamp("update_at");

                    list.add(new Announcement(
                            rs.getString("uuid"),
                            rs.getString("author_uuid"),
                            rs.getString("author"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("priority"),
                            (imageBytes != null && imageBytes.length > 0) ? ImageConverter.toImage(imageBytes) : null,
                            rs.getTimestamp("sendAt").toLocalDateTime(),
                            updateAt != null ? updateAt.toLocalDateTime() : null,
                            rs.getInt("like_count"),
                            rs.getInt("liked_by_me") == 1,
                            rs.getInt("comment_count")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudieron obtener los anuncios", e);
        }

        return list;
    }

    public static boolean toggleLike(String announceUuid, String userUuid) {

        Connection conn = connection();

        try {
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM announce_likes WHERE announce_uuid = ? AND user_uuid = ?")) {
                del.setString(1, announceUuid);
                del.setString(2, userUuid);
                if (del.executeUpdate() > 0) {
                    return false;
                }
            }

            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO announce_likes (announce_uuid, user_uuid) VALUES (?, ?)")) {
                ins.setString(1, announceUuid);
                ins.setString(2, userUuid);
                ins.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo actualizar el like del anuncio " + announceUuid, e);
        }
    }

    public static void insertComment(AnnouncementComment c) {

        String sql = """
        INSERT INTO announce_comments (uuid, announce_uuid, user_uuid, content, createAt)
        VALUES (?, ?, ?, ?, ?)
        """;

        Connection conn = connection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getUuid());
            stmt.setString(2, c.getAnnounceUuid());
            stmt.setString(3, c.getAuthorUuid());
            stmt.setString(4, c.getContent());
            stmt.setTimestamp(5, Timestamp.valueOf(c.getCreateAt()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo guardar el comentario", e);
        }
    }

    public static void updateComment(String commentUuid, String content) {

        String sql = "UPDATE announce_comments SET content = ? WHERE uuid = ?";

        Connection conn = connection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, content);
            stmt.setString(2, commentUuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo editar el comentario", e);
        }
    }

    public static void deleteComment(String commentUuid) {

        String sql = "DELETE FROM announce_comments WHERE uuid = ?";

        Connection conn = connection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, commentUuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo eliminar el comentario", e);
        }
    }

    public static List<AnnouncementComment> getComments(String announceUuid) {

        String sql = """
        SELECT c.uuid, c.announce_uuid, c.user_uuid, u.display AS author_name, c.content, c.createAt
        FROM announce_comments c
        INNER JOIN users u ON u.uuid = c.user_uuid
        WHERE c.announce_uuid = ?
        ORDER BY c.createAt ASC
        """;

        Connection conn = connection();
        List<AnnouncementComment> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, announceUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AnnouncementComment(
                            rs.getString("uuid"),
                            rs.getString("announce_uuid"),
                            rs.getString("user_uuid"),
                            rs.getString("author_name"),
                            rs.getString("content"),
                            rs.getTimestamp("createAt").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudieron obtener los comentarios", e);
        }

        return list;
    }
}
