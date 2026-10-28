package org.example.servinet.application.usecase.announce;

import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlAnnounceRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.service.AnnounceServices;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.AnnouncementComment;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;
import java.time.LocalDateTime;
import java.util.UUID;

public class EventsAnnounceUseCase {

    private final AuthorizationSecurity authorizationService;
    private final ActivityLogsService activityLogsService;

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    private final InMemoryRepository<Announcement> announcementInMemoryRepository;
    private final SqlAnnounceRepository announcementSqlRepository;

    public EventsAnnounceUseCase(AuthorizationSecurity authorizationService, ActivityLogsService activityLogsService, InMemoryComplexRepository<Staff> staffInMemoryRepository, InMemoryRepository<Announcement> announcementInMemoryRepository, SqlAnnounceRepository announcementSqlRepository) {
        this.authorizationService = authorizationService;
        this.activityLogsService = activityLogsService;
        this.staffInMemoryRepository = staffInMemoryRepository;
        this.announcementInMemoryRepository = announcementInMemoryRepository;

        this.announcementSqlRepository = announcementSqlRepository;
    }

    public void toggleLike(Announcement target) {

        if (!authorizationService.hasPermission(Permission.ANNOUNCE_VIEW))
            throw new PermissionException("No tienes permiso para reaccionar a los anuncios");

        boolean nowLiked = announcementSqlRepository.toggleLike(target.getUuid(), staffInMemoryRepository.getCurrent().getUuid());

        target.setLikedByMe(nowLiked);
        target.setLikeCount(Math.max(0, target.getLikeCount() + (nowLiked ? 1 : -1)));
    }

    public AnnouncementComment addComment(Announcement target, String content) {

        if (!authorizationService.hasPermission(Permission.ANNOUNCE_VIEW))
            throw new PermissionException("No tienes permiso para comentar anuncios");

        String clean = content == null ? "" : content.trim();
        if (clean.isEmpty())
            throw new InvalidValueException("El comentario no puede estar vacío.");
        if (clean.length() > AnnounceServices.COMMENT_MAX)
            throw new InvalidValueException("El comentario no puede superar los " + AnnounceServices.COMMENT_MAX + " caracteres.");

        Staff staffCurrent = staffInMemoryRepository.getCurrent();

        AnnouncementComment comment = new AnnouncementComment(
                UUID.randomUUID().toString(),
                target.getUuid(),
                staffCurrent.getUuid(),
                staffCurrent.getDisplay(),
                clean,
                LocalDateTime.now()
        );

        announcementSqlRepository.insertComment(comment);
        target.setCommentCount(target.getCommentCount() + 1);

        return comment;
    }

    public boolean canEditComment(AnnouncementComment c) {
        return c.getAuthorUuid() != null && c.getAuthorUuid().equalsIgnoreCase(
                staffInMemoryRepository.getCurrent().getUuid());
    }

    public boolean canDeleteComment(AnnouncementComment c) {
        return canEditComment(c) || authorizationService.hasPermission(Permission.ANNOUNCE_DELETE);
    }

    public void editComment(AnnouncementComment comment, String content) {

        if (!canEditComment(comment))
            throw new PermissionException("Solo puedes editar tus propios comentarios");

        String clean = content == null ? "" : content.trim();
        if (clean.isEmpty())
            throw new InvalidValueException("El comentario no puede estar vacío.");
        if (clean.length() > AnnounceServices.COMMENT_MAX)
            throw new InvalidValueException("El comentario no puede superar los " + AnnounceServices.COMMENT_MAX + " caracteres.");

        announcementSqlRepository.updateComment(comment.getUuid(), clean);
        comment.setContent(clean);
    }

    public void deleteComment(Announcement target, AnnouncementComment comment) {

        if (!canDeleteComment(comment))
            throw new PermissionException("No tienes permiso para eliminar este comentario");

        announcementSqlRepository.deleteComment(comment.getUuid());
        target.setCommentCount(Math.max(0, target.getCommentCount() - 1));
    }

    public void deleteAnnouncement(Announcement target) {

        if (!canDelete(target))
            throw new PermissionException("No tienes permiso para eliminar este anuncio");

        announcementSqlRepository.delete(target.getUuid());
        activityLogsService.addLog(LogType.ANNOUNCE_DELETE, "Eliminó el anuncio \"" + target.getTitle() + "\"");
    }

    public boolean canDelete(Announcement a) {

        return a != null
                && authorizationService.hasPermission(
                Permission.ANNOUNCE_VIEW
        )
                && authorizationService.hasPermission(
                Permission.ANNOUNCE_DELETE
        );
    }
}
