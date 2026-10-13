package org.example.servinet.core.application.usecase;

import org.example.servinet.core.application.dto.AnnouncementDto;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.application.service.UuidGenerator;
import org.example.servinet.core.domain.entities.Announcement;
import org.example.servinet.core.domain.entities.AnnouncementComment;
import org.example.servinet.core.domain.enums.LogType;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.FileExistException;
import org.example.servinet.core.domain.exception.InvalidValueException;
import org.example.servinet.core.domain.exception.RoleNoPermission;
import org.example.servinet.infrastructure.database.models.AnnouncementModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementsUseCase {

    public static final int TITLE_MAX = 20;
    public static final int DESCRIPTION_MAX = 150;
    public static final int COMMENT_MAX = 500;
    private static final long IMAGE_MAX_BYTES = 5L * 1024 * 1024;

    public static final List<String> PRIORITIES = List.of("Alta", "Media", "Baja");

    private static List<Announcement> announcements = new ArrayList<>();

    public static List<Announcement> getAllAnnouncements() {
        return announcements;
    }

    public static void loadAnnouncements() {

        if (!PermissionValidation.hasPermission(Permission.ANNOUNCE_VIEW)) {
            announcements = new ArrayList<>();
            return;
        }

        announcements = AnnouncementModel.getAll(SessionUseCase.getUserUuid());
    }

    private static boolean isAuthor(Announcement a) {
        return a.getAuthorUuid() != null && a.getAuthorUuid().equalsIgnoreCase(SessionUseCase.getUserUuid());
    }

    public static boolean canEdit(Announcement a) {
        return isAuthor(a) || PermissionValidation.hasPermission(Permission.ANNOUNCE_EDIT);
    }

    public static boolean canDelete(Announcement a) {
        return isAuthor(a) || PermissionValidation.hasPermission(Permission.ANNOUNCE_DELETE);
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

    private static byte[] readImage(Path image) {
        try {
            if (Files.size(image) > IMAGE_MAX_BYTES)
                throw new InvalidValueException("La imagen no puede pesar más de 5 MB.");
            return Files.readAllBytes(image);
        } catch (IOException e) {
            throw new FileExistException(image);
        }
    }

    public static void createAnnouncement(AnnouncementDto dto) {

        if (!PermissionValidation.hasPermission(Permission.ANNOUNCE_CREATE))
            throw new RoleNoPermission("No tienes permiso para crear anuncios");

        validate(dto);

        byte[] imageBytes = dto.getImage() != null ? readImage(dto.getImage()) : null;

        String authorName = SessionUseCase.getUserName();
        if (authorName.length() > TITLE_MAX) authorName = authorName.substring(0, TITLE_MAX);

        Announcement announcement = new Announcement(
                new GenerateIdUseCase(new UuidGenerator()).execute(),
                SessionUseCase.getUserUuid(),
                authorName,
                dto.getTitle().trim(),
                dto.getDescription().trim(),
                dto.getPriority(),
                null,
                LocalDateTime.now(),
                null,
                0, false, 0
        );

        AnnouncementModel.insert(announcement, imageBytes);
        LogsUseCase.addLog(LogType.ANNOUNCE_CREATE, "Publicó el anuncio \"" + announcement.getTitle() + "\"");
        loadAnnouncements();
    }

    public static void editAnnouncement(Announcement target, AnnouncementDto dto) {

        if (!canEdit(target))
            throw new RoleNoPermission("No tienes permiso para editar este anuncio");

        validate(dto);

        boolean replaceImage = dto.getImage() != null;
        byte[] imageBytes = replaceImage ? readImage(dto.getImage()) : null;

        AnnouncementModel.update(
                target.getUuid(),
                dto.getTitle().trim(),
                dto.getDescription().trim(),
                dto.getPriority(),
                imageBytes,
                replaceImage
        );

        LogsUseCase.addLog(LogType.ANNOUNCE_EDIT, "Editó el anuncio \"" + dto.getTitle().trim() + "\"");
        loadAnnouncements();
    }

    public static void deleteAnnouncement(Announcement target) {

        if (!canDelete(target))
            throw new RoleNoPermission("No tienes permiso para eliminar este anuncio");

        AnnouncementModel.delete(target.getUuid());
        LogsUseCase.addLog(LogType.ANNOUNCE_DELETE, "Eliminó el anuncio \"" + target.getTitle() + "\"");
        loadAnnouncements();
    }

    public static void toggleLike(Announcement target) {

        if (!PermissionValidation.hasPermission(Permission.ANNOUNCE_VIEW))
            throw new RoleNoPermission("No tienes permiso para reaccionar a los anuncios");

        boolean nowLiked = AnnouncementModel.toggleLike(target.getUuid(), SessionUseCase.getUserUuid());

        target.setLikedByMe(nowLiked);
        target.setLikeCount(Math.max(0, target.getLikeCount() + (nowLiked ? 1 : -1)));
    }

    public static List<AnnouncementComment> getComments(Announcement target) {
        return AnnouncementModel.getComments(target.getUuid());
    }

    public static AnnouncementComment addComment(Announcement target, String content) {

        if (!PermissionValidation.hasPermission(Permission.ANNOUNCE_VIEW))
            throw new RoleNoPermission("No tienes permiso para comentar anuncios");

        String clean = content == null ? "" : content.trim();
        if (clean.isEmpty())
            throw new InvalidValueException("El comentario no puede estar vacío.");
        if (clean.length() > COMMENT_MAX)
            throw new InvalidValueException("El comentario no puede superar los " + COMMENT_MAX + " caracteres.");

        AnnouncementComment comment = new AnnouncementComment(
                new GenerateIdUseCase(new UuidGenerator()).execute(),
                target.getUuid(),
                SessionUseCase.getUserUuid(),
                SessionUseCase.getUserName(),
                clean,
                LocalDateTime.now()
        );

        AnnouncementModel.insertComment(comment);
        target.setCommentCount(target.getCommentCount() + 1);

        return comment;
    }

    public static boolean canEditComment(AnnouncementComment c) {
        return c.getAuthorUuid() != null && c.getAuthorUuid().equalsIgnoreCase(SessionUseCase.getUserUuid());
    }

    public static boolean canDeleteComment(AnnouncementComment c) {
        return canEditComment(c) || PermissionValidation.hasPermission(Permission.ANNOUNCE_DELETE);
    }

    public static void editComment(AnnouncementComment comment, String content) {

        if (!canEditComment(comment))
            throw new RoleNoPermission("Solo puedes editar tus propios comentarios");

        String clean = content == null ? "" : content.trim();
        if (clean.isEmpty())
            throw new InvalidValueException("El comentario no puede estar vacío.");
        if (clean.length() > COMMENT_MAX)
            throw new InvalidValueException("El comentario no puede superar los " + COMMENT_MAX + " caracteres.");

        AnnouncementModel.updateComment(comment.getUuid(), clean);
        comment.setContent(clean);
    }

    public static void deleteComment(Announcement target, AnnouncementComment comment) {

        if (!canDeleteComment(comment))
            throw new RoleNoPermission("No tienes permiso para eliminar este comentario");

        AnnouncementModel.deleteComment(comment.getUuid());
        target.setCommentCount(Math.max(0, target.getCommentCount() - 1));
    }
}
