package org.example.servinet.infrastructure.database.sqlserver;

import org.example.servinet.application.port.out.sql.AnnounceRepository;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.AnnouncementComment;
import org.example.servinet.domain.exception.DatabaseException;
import org.example.servinet.infrastructure.database.DatabaseFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlAnnouncementRepository implements AnnounceRepository {
    private final DatabaseFactory databaseFactory;

    public SqlAnnouncementRepository(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public void add(Announcement a) {

        String sql = "{call dbo.sp_AddAnnounces(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {


            stmt.setString(1, a.getUuid());
            stmt.setString(2, a.getPriority());
            stmt.setString(3, a.getTitle());
            stmt.setString(4, a.getDescription());
            stmt.setString(5, a.getAuthorName());
            stmt.setString(6, a.getAuthorUuid());
            stmt.setBytes(7, a.getImage());
            stmt.setTimestamp(8, Timestamp.valueOf(a.getSendAt()));
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
        String sql = "{call dbo.sp_DeleteAnnounce(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, uuid);
            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar el anuncio " + uuid, e
            );
        }
    }

    @Override
    public void update(Announcement entity) {
        String sql = "{call dbo.sp_UpdateAnnounce(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, entity.getUuid());
            stmt.setString(2, entity.getTitle());
            stmt.setString(3, entity.getDescription());
            stmt.setString(4, entity.getPriority());

            byte[] image = entity.getImage();
            boolean replaceImage = image != null && image.length > 0;

            stmt.setBoolean(5, replaceImage);

            if (replaceImage) {
                stmt.setBytes(6, image);
            } else {
                stmt.setNull(6, java.sql.Types.VARBINARY);
            }

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el anuncio "
                            + entity.getUuid(), e
            );
        }
    }

    @Override
    public List<Announcement> getPage(String userUuid, int offset, int pageSize) {

        if (offset < 0 || pageSize < 1) {
            throw new IllegalArgumentException(
                    "El offset no puede ser negativo y pageSize debe ser positivo."
            );
        }

        String sql = "{call dbo.sp_GetAnnouncesPage(?, ?, ?)}";
        List<Announcement> list = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, userUuid);
            stmt.setInt(2, offset);
            stmt.setInt(3, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    byte[] imageBytes = rs.getBytes("image");
                    Timestamp updateAt = rs.getTimestamp("update_at");
                    Timestamp sendAt = rs.getTimestamp("sendAt");

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
                            sendAt.toLocalDateTime(),
                            updateAt != null
                                    ? updateAt.toLocalDateTime()
                                    : null,
                            rs.getInt("like_count"),
                            rs.getInt("liked_by_me") == 1,
                            rs.getInt("comment_count")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudieron obtener los anuncios.", e
            );
        }

        return list;
    }


    @Override
    public boolean toggleLike(String announceUuid, String userUuid) {
        String sql = "{call dbo.sp_ToggleAnnounceLike(?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, announceUuid);
            stmt.setString(2, userUuid);
            stmt.registerOutParameter(3, java.sql.Types.BIT);

            stmt.execute();

            return stmt.getBoolean(3);

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo actualizar el like del anuncio " + announceUuid,
                    e
            );
        }
    }
    @Override
    public void insertComment(AnnouncementComment c) {
        String sql = "{call dbo.sp_AddAnnounceComment(?, ?, ?, ?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, c.getUuid());
            stmt.setString(2, c.getAnnounceUuid());
            stmt.setString(3, c.getAuthorUuid());
            stmt.setString(4, c.getContent());
            stmt.setTimestamp(5, Timestamp.valueOf(c.getCreateAt()));

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo guardar el comentario", e
            );
        }
    }
    @Override
    public void updateComment(String commentUuid, String content) {
        String sql = "{call dbo.sp_UpdateAnnounceComment(?, ?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, commentUuid);
            stmt.setString(2, content);

            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo editar el comentario " + commentUuid, e
            );
        }
    }
    @Override
    public void deleteComment(String commentUuid) {
        String sql = "{call dbo.sp_DeleteAnnounceComment(?)}";

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, commentUuid);
            stmt.execute();

        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo eliminar el comentario " + commentUuid, e
            );
        }
    }

    @Override
    public List<AnnouncementComment> getComments(String announceUuid) {
        String sql = "{call dbo.sp_GetAnnounceComments(?)}";
        List<AnnouncementComment> list = new ArrayList<>();

        try (Connection conn = databaseFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, announceUuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp createAt = rs.getTimestamp("createAt");

                    list.add(new AnnouncementComment(
                            rs.getString("uuid"),
                            rs.getString("announce_uuid"),
                            rs.getString("user_uuid"),
                            rs.getString("author_name"),
                            rs.getString("content"),
                            createAt.toLocalDateTime()
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
