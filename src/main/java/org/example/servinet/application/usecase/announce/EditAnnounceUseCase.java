package org.example.servinet.application.usecase.announce;

import org.example.servinet.application.dto.AnnouncementDto;
import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlAnnounceRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.service.AnnounceServices;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.AnnounceException;
import org.example.servinet.domain.exception.FileExistException;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EditAnnounceUseCase {

    private final AuthorizationSecurity authorizationService;
    private final ActivityLogsService activityLogsService;

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    private final InMemoryRepository<Announcement> announcementInMemoryRepository;
    private final SqlAnnounceRepository announcementSqlRepository;

    public EditAnnounceUseCase(AuthorizationSecurity authorizationService, ActivityLogsService activityLogsService, InMemoryComplexRepository<Staff> staffInMemoryRepository, InMemoryRepository<Announcement> announcementInMemoryRepository, SqlAnnounceRepository announcementSqlRepository) {
        this.authorizationService = authorizationService;
        this.activityLogsService = activityLogsService;
        this.staffInMemoryRepository = staffInMemoryRepository;
        this.announcementInMemoryRepository = announcementInMemoryRepository;

        this.announcementSqlRepository = announcementSqlRepository;
    }


    public void editAnnouncement(Announcement target, AnnouncementDto dto) {

        if (!authorizationService.hasPermission(Permission.ANNOUNCE_EDIT))
            throw new PermissionException("No tienes permiso para editar este anuncio");

        if (!staffInMemoryRepository.getCurrent().getUuid().equals(target.getAuthorUuid())){
            throw new AnnounceException("No tienes permiso para editar el mensaje");
        }

        AnnounceServices.validate(dto);

        boolean replaceImage = dto.getImage() != null;
        byte[] imageBytes = replaceImage ? readImage(dto.getImage()) : null;

        Announcement annon = new Announcement(
                target.getUuid(),
                target.getAuthorUuid(),
                target.getAuthorName(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getPriority(),
                imageBytes,
                target.getSendAt(),
                target.getUpdateAt(),
                target.getLikeCount(),
                target.isLikedByMe(),
                target.getCommentCount()
        );


        announcementSqlRepository.update(annon);
        announcementInMemoryRepository.remove(target.getUuid());
        announcementInMemoryRepository.addEntity(annon);

        activityLogsService.addLog(LogType.ANNOUNCE_EDIT, "Editó el anuncio \"" + dto.getTitle().trim() + "\"");
    }

    private byte[] readImage(Path image) {
        try {
            if (Files.size(image) > AnnounceServices.IMAGE_MAX_BYTES)
                throw new InvalidValueException("La imagen no puede pesar más de 5 MB.");
            return Files.readAllBytes(image);
        } catch (IOException e) {
            throw new FileExistException(image);
        }
    }
}
