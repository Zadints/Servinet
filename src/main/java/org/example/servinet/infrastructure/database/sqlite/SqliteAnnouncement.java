package org.example.servinet.infrastructure.database.sqlite;

import org.example.servinet.application.port.out.sql.SqlAnnounceRepository;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.AnnouncementComment;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.deploy.factory.DatabaseFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqliteAnnouncement implements SqlAnnounceRepository {
    private final DatabaseFactory databaseFactory;

    public SqliteAnnouncement(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void add(Announcement a) {

        String sql = "INSERT INTO announce (uuid, priority, title, description, author, author_uuid, image, sendAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.getUuid());
            stmt.setString(2, a.getPriority());
            stmt.setString(3, a.getTitle());
            stmt.setString(4, a.getDescription());
            stmt.setString(5, a.getAuthorName());
            stmt.setString(6, a.getAuthorUuid());
            SqliteUtil.setNullableBytes(stmt, 7, a.getImage());
            stmt.setString(8, SqliteUtil.toDb(a.getSendAt()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("No se pudo guardar el anuncio", e);
        }
    }

    @Override
    public Announcement get(String id) {
        return null;
    }

    public void delete(String uuid) {
        try (Connection conn = databaseFactory.getConnection()) {

            SqliteUtil.runInTransaction(conn, c -> {
                SqliteUtil.update(c, "DELETE FROM announce_likes WHERE announce_uuid = ?", uuid);
                SqliteUtil.update(c, "DELETE FROM announce_comments WHERE announce_uuid = ?", uuid);

                if (SqliteUtil.update(c, "DELETE FROM announce WHERE uuid = ?", uuid) == 0) {
                    throw new DatabaseException("No se encontró el anuncio que se desea eliminar.");
                }
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar el anuncio " + uuid, e
            );
        }
    }

    @Override
    public void update(Announcement entity) {
        String sql = "UPDATE announce SET title = ?, description = ?, priority = ?, "
                + "image = CASE WHEN ? = 1 THEN ? ELSE image END, update_at = ? "
                + "WHERE uuid = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            byte[] image = entity.getImage();
            boolean replaceImage = image != null && image.length > 0;

            stmt.setString(1, entity.getTitle());
            stmt.setString(2, entity.getDescription());
            stmt.setString(3, entity.getPriority());
            stmt.setInt(4, replaceImage ? 1 : 0);
            SqliteUtil.setNullableBytes(stmt, 5, replaceImage ? image : null);
            stmt.setString(6, SqliteUtil.now());
            stmt.setString(7, entity.getUuid());

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException("No se encontró el anuncio que se desea actualizar.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el anuncio "
                            + entity.getUuid(), e
            );
        }
    }

    @Override
    public BigDecimal getAllMoney() {
        return null;
    }

    @Override
    public Announcement getAll() {
        return null;
    }

    @Override
    public List<Announcement> getPage(String userUuid, int offset, int pageSize) {

        if (offset < 0 || pageSize < 1) {

            System.out.println("No podemos buscar de : " + offset + " a " + pageSize);
            throw new IllegalArgumentException(
                    "El offset no puede ser negativo y pageSize debe ser positivo."
            );
        }
        System.out.println("Buscando usuario : " + offset + " a " + pageSize);

        String sql = "SELECT a.uuid, a.priority, a.title, a.description, a.author, a.author_uuid, "
                + "a.image, a.sendAt, a.update_at, "
                + "(SELECT COUNT(*) FROM announce_likes l WHERE l.announce_uuid = a.uuid) AS like_count, "
                + "(SELECT COUNT(*) FROM announce_comments c WHERE c.announce_uuid = a.uuid) AS comment_count, "
                + "CASE WHEN EXISTS (SELECT 1 FROM announce_likes l2 "
                + "WHERE l2.announce_uuid = a.uuid AND l2.user_uuid = ?) THEN 1 ELSE 0 END AS liked_by_me "
                + "FROM announce a ORDER BY a.sendAt DESC, a.uuid DESC LIMIT ? OFFSET ?";
        List<Announcement> list = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userUuid);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    byte[] imageBytes = rs.getBytes("image");

                    list.add(new Announcement(
                            rs.getString("uuid"),
                            rs.getString("author_uuid"),
                            rs.getString("author"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("priority"),
                            imageBytes != null && imageBytes.length > 0
                                    ? imageBytes
                                    : null,
                            SqliteUtil.getDateTime(rs, "sendAt"),
                            SqliteUtil.getDateTime(rs, "update_at"),
                            rs.getInt("like_count"),
                            rs.getInt("liked_by_me") == 1,
                            rs.getInt("comment_count")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DatabaseException(
                    "No se pudieron obtener los anuncios.", e
            );
        }

        return list;
    }

    @Override
    public boolean toggleLike(String announceUuid, String userUuid) {
        try (Connection conn = databaseFactory.getConnection()) {

            return SqliteUtil.inTransaction(conn, c -> {
                int removed = SqliteUtil.update(
                        c,
                        "DELETE FROM announce_likes WHERE announce_uuid = ? AND user_uuid = ?",
                        announceUuid, userUuid
                );

                if (removed > 0) {
                    return false;
                }

                SqliteUtil.update(
                        c,
                        "INSERT INTO announce_likes (announce_uuid, user_uuid) VALUES (?, ?)",
                        announceUuid, userUuid
                );
                return true;
            });

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el like del anuncio " + announceUuid,
                    e
            );
        }
    }

    @Override
    public void insertComment(AnnouncementComment c) {
        String sql = "INSERT INTO announce_comments (uuid, announce_uuid, user_uuid, content, createAt) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getUuid());
            stmt.setString(2, c.getAnnounceUuid());
            stmt.setString(3, c.getAuthorUuid());
            stmt.setString(4, c.getContent());
            stmt.setString(5, SqliteUtil.toDb(c.getCreateAt()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo guardar el comentario", e
            );
        }
    }

    @Override
    public void updateComment(String commentUuid, String content) {
        String sql = "UPDATE announce_comments SET content = ? WHERE uuid = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, content);
            stmt.setString(2, commentUuid);

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException("No se encontró el comentario.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo editar el comentario " + commentUuid, e
            );
        }
    }

    @Override
    public void deleteComment(String commentUuid) {
        String sql = "DELETE FROM announce_comments WHERE uuid = ?";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, commentUuid);

            if (stmt.executeUpdate() == 0) {
                throw new DatabaseException("No se encontró el comentario.");
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar el comentario " + commentUuid, e
            );
        }
    }

    @Override
    public List<AnnouncementComment> getComments(String announceUuid) {
        String sql = "SELECT c.uuid, c.announce_uuid, c.user_uuid, u.display AS author_name, "
                + "c.content, c.createAt "
                + "FROM announce_comments c INNER JOIN users u ON u.uuid = c.user_uuid "
                + "WHERE c.announce_uuid = ? ORDER BY c.createAt ASC, c.uuid ASC";
        List<AnnouncementComment> list = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, announceUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AnnouncementComment(
                            rs.getString("uuid"),
                            rs.getString("announce_uuid"),
                            rs.getString("user_uuid"),
                            rs.getString("author_name"),
                            rs.getString("content"),
                            SqliteUtil.getDateTime(rs, "createAt")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los comentarios del anuncio "
                            + announceUuid, e
            );
        }

        return list;
    }
}
