package org.example.servinet.application.usecase.announce;

import org.example.servinet.application.dto.AnnouncementDto;
import org.example.servinet.application.security.AuthorizationService;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.GenerateIdUseCase;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.AnnouncementComment;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.infrastructure.database.sqlserver.SqlAnnouncementRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementsUseCase {



    private static List<Announcement> announcements = new ArrayList<>();

    public static List<Announcement> getAllAnnouncements() {

        if (!AuthorizationService.hasPermission(
                Permission.ANNOUNCE_VIEW)) {

            return List.of();
        }

        return List.copyOf(announcements);
    }

    public static void loadAnnouncements() {

        if (!AuthorizationService.hasPermission(Permission.ANNOUNCE_VIEW)) {
            announcements = new ArrayList<>();
            return;
        }

        announcements = SqlAnnouncementRepository.getAll(LoginStaffUseCase.getUserUuid());
    }

    private static boolean isAuthor(Announcement a) {
        return a.getAuthorUuid() != null && a.getAuthorUuid().equalsIgnoreCase(LoginStaffUseCase.getUserUuid());
    }

    public static boolean canEdit(Announcement a) {

        return a != null
                && AuthorizationService.hasPermission(
                Permission.ANNOUNCE_VIEW
        )
                && AuthorizationService.hasPermission(
                Permission.ANNOUNCE_EDIT
        );
    }

    public static boolean canDelete(Announcement a) {

        return a != null
                && AuthorizationService.hasPermission(
                Permission.ANNOUNCE_VIEW
        )
                && AuthorizationService.hasPermission(
                Permission.ANNOUNCE_DELETE
        );
    }

    private static void validate(AnnouncementDto dto) {

        if (dto.getTitle() == null || dto.getTitle().isBlank())
            throw new InvalidValueException("El título no puede estar vacío.");
        if (dto.getTitle().trim().length() > TITLE_MAX)
            throw new InvalidValueException("El título no puede superar los " + TITLE_MAX + " caracteres.");

        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new InvalidValueException("El contenido no puede estar vacío.");
        if (dto.getDescription().trim().length() > DESCRIPTION_MAX)
            throw new InvalidValueException("El contenido no puede superar los " + DESCRIPTION_MAX + " caracteres.");

        if (dto.getPriority() == null || !PRIORITIES.contains(dto.getPriority()))
            throw new InvalidValueException("Selecciona la prioridad del anuncio.");
    }





    public static void deleteAnnouncement(Announcement target) {

        if (!canDelete(target))
            throw new PermissionException("No tienes permiso para eliminar este anuncio");

        SqlAnnouncementRepository.delete(target.getUuid());
        ActivityLogsService.addLog(LogType.ANNOUNCE_DELETE, "Eliminó el anuncio \"" + target.getTitle() + "\"");
        loadAnnouncements();
    }

    public static void toggleLike(Announcement target) {

        if (!AuthorizationService.hasPermission(Permission.ANNOUNCE_VIEW))
            throw new PermissionException("No tienes permiso para reaccionar a los anuncios");

        boolean nowLiked = SqlAnnouncementRepository.toggleLike(target.getUuid(), LoginStaffUseCase.getUserUuid());

        target.setLikedByMe(nowLiked);
        target.setLikeCount(Math.max(0, target.getLikeCount() + (nowLiked ? 1 : -1)));
    }

    public static List<AnnouncementComment> getComments(
            Announcement target) {

        if (target == null ||
                !AuthorizationService.hasPermission(
                        Permission.ANNOUNCE_VIEW)) {

            throw new PermissionException(
                    "No tienes permiso para consultar comentarios."
            );
        }

        return SqlAnnouncementRepository.getComments(
                target.getUuid()
        );
    }

    public static AnnouncementComment addComment(Announcement target, String content) {

        if (!AuthorizationService.hasPermission(Permission.ANNOUNCE_VIEW))
            throw new PermissionException("No tienes permiso para comentar anuncios");

        String clean = content == null ? "" : content.trim();
        if (clean.isEmpty())
            throw new InvalidValueException("El comentario no puede estar vacío.");
        if (clean.length() > COMMENT_MAX)
            throw new InvalidValueException("El comentario no puede superar los " + COMMENT_MAX + " caracteres.");

        AnnouncementComment comment = new AnnouncementComment(
                new GenerateIdUseCase(new UuidGenerator()).execute(),
                target.getUuid(),
                LoginStaffUseCase.getUserUuid(),
                LoginStaffUseCase.getUserName(),
                clean,
                LocalDateTime.now()
        );

        SqlAnnouncementRepository.insertComment(comment);
        target.setCommentCount(target.getCommentCount() + 1);

        return comment;
    }

    public static boolean canEditComment(AnnouncementComment c) {
        return c.getAuthorUuid() != null && c.getAuthorUuid().equalsIgnoreCase(LoginStaffUseCase.getUserUuid());
    }

    public static boolean canDeleteComment(AnnouncementComment c) {
        return canEditComment(c) || AuthorizationService.hasPermission(Permission.ANNOUNCE_DELETE);
    }

    public static void editComment(AnnouncementComment comment, String content) {

        if (!canEditComment(comment))
            throw new PermissionException("Solo puedes editar tus propios comentarios");

        String clean = content == null ? "" : content.trim();
        if (clean.isEmpty())
            throw new InvalidValueException("El comentario no puede estar vacío.");
        if (clean.length() > COMMENT_MAX)
            throw new InvalidValueException("El comentario no puede superar los " + COMMENT_MAX + " caracteres.");

        SqlAnnouncementRepository.updateComment(comment.getUuid(), clean);
        comment.setContent(clean);
    }

    public static void deleteComment(Announcement target, AnnouncementComment comment) {

        if (!canDeleteComment(comment))
            throw new PermissionException("No tienes permiso para eliminar este comentario");

        SqlAnnouncementRepository.deleteComment(comment.getUuid());
        target.setCommentCount(Math.max(0, target.getCommentCount() - 1));
    }
}
