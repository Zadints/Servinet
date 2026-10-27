package org.example.servinet.application.usecase.announce;

import org.example.servinet.application.dto.AnnouncementDto;
import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlRepository;
import org.example.servinet.application.security.AuthorizationService;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.service.AnnounceServices;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.FileExistException;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.infrastructure.database.sqlserver.SqlAnnouncementRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateAnnounceUseCase {

    private final AuthorizationService authorizationService;
    private final ActivityLogsService activityLogsService;

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    private final InMemoryRepository<Announcement> announcementInMemoryRepository;
    private final SqlRepository<Announcement> announcementSqlRepository;

    public CreateAnnounceUseCase(AuthorizationService authorizationService, ActivityLogsService activityLogsService, InMemoryComplexRepository<Staff> staffInMemoryRepository, InMemoryRepository<Announcement> announcementInMemoryRepository, SqlRepository<Announcement> announcementSqlRepository) {
        this.authorizationService = authorizationService;
        this.activityLogsService = activityLogsService;
        this.staffInMemoryRepository = staffInMemoryRepository;
        this.announcementInMemoryRepository = announcementInMemoryRepository;
        this.announcementSqlRepository = announcementSqlRepository;
    }

    public void createAnnouncement(AnnouncementDto dto) {

        if (!authorizationService.hasPermission(Permission.ANNOUNCE_CREATE))
            throw new PermissionException("No tienes permiso para crear anuncios");

        AnnounceServices.validate(dto);

        byte[] imageBytes = dto.getImage() != null ? readImage(dto.getImage()) : null;

        Staff staffCurrent = staffInMemoryRepository.getCurrent();

        Announcement announcement = new Announcement(
                UUID.randomUUID().toString(),
                staffCurrent.getUuid(),
                staffCurrent.getDisplay(),
                dto.getTitle().trim(),
                dto.getDescription().trim(),
                dto.getPriority(),
                imageBytes,
                LocalDateTime.now(),
                null,
                0,
                false,
                0
        );

        announcementInMemoryRepository.addEntity(announcement);
        announcementSqlRepository.add(announcement);
        activityLogsService.addLog(LogType.ANNOUNCE_CREATE, "Publicó el anuncio \"" + announcement.getTitle() + "\"");
    }

    private byte[] readImage(Path image) {
        try {
            if (Files.size(image) > AnnounceServices.getImageMaxBytes())
                throw new InvalidValueException("La imagen no puede pesar más de 5 MB.");
            return Files.readAllBytes(image);
        } catch (IOException e) {
            throw new FileExistException(image);
        }
    }
}
