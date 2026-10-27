package org.example.servinet.application.usecase.announce;

import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlRepository;
import org.example.servinet.application.security.AuthorizationService;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.infrastructure.database.sqlserver.SqlAnnouncementRepository;

import java.util.ArrayList;
import java.util.List;

public class GetAnnounceUseCase {

    /**
     * me refería a como solucionan esto pq a ver si
     * la app va a mostrar +100 anuncios, cargar en el momento
     * que renderiza la gui, será muy lento.
     *
     * Se me ocurre que carga los últimos 5 y muestra miestras en resto
     * coloca una opcion en el gui que diga cargar más mensajes y de esta
     * forma evitamso saturar ram. pero ojitooo los 5 cargados se quedan en memoria
     * permanente peroo los demás se cargan en memoria temporal y se eliminan luego
     * de cambiar gui y evitamos saturar ram.
     **/

    private final AuthorizationService authorizationService;
    private final ActivityLogsService activityLogsService;

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    private final InMemoryRepository<Announcement> announcementInMemoryRepository;
    private final SqlRepository<Announcement> announcementSqlRepository;

    public EditAnnounceUseCase(AuthorizationService authorizationService,  ActivityLogsService activityLogsService, InMemoryComplexRepository<Staff> staffInMemoryRepository, InMemoryRepository<Announcement> announcementInMemoryRepository, SqlRepository<Announcement> announcementSqlRepository) {
        this.authorizationService = authorizationService;
        this.activityLogsService = activityLogsService;
        this.staffInMemoryRepository = staffInMemoryRepository;
        this.announcementInMemoryRepository = announcementInMemoryRepository;
        this.announcementSqlRepository = announcementSqlRepository;
    }

    public List<Announcement> loadAnnouncements() {

        if (!authorizationService.hasPermission(Permission.ANNOUNCE_VIEW)) {
            throw new PermissionException("No tienes permiso para ver anuncios");
        }
        List<Announcement> announcement = announcementSqlRepository.get("5");

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

    public static List<Announcement> getAllAnnouncements() {

        if (!AuthorizationService.hasPermission(
                Permission.ANNOUNCE_VIEW)) {

            return List.of();
        }

        return List.copyOf(announcements);
    }
}
