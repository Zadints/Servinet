package org.example.servinet.application.usecase.announce;

import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlAnnounceRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.AnnouncementComment;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;

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

    private final AuthorizationSecurity authorizationService;

    private final InMemoryComplexRepository<Staff> staffInMemoryRepository;
    private final InMemoryRepository<Announcement> announcementInMemoryRepository;
    private final SqlAnnounceRepository announcementSqlRepository;

    public GetAnnounceUseCase(AuthorizationSecurity authorizationService, InMemoryComplexRepository<Staff> staffInMemoryRepository, InMemoryRepository<Announcement> announcementInMemoryRepository, SqlAnnounceRepository announcementSqlRepository) {
        this.authorizationService = authorizationService;
        this.staffInMemoryRepository = staffInMemoryRepository;
        this.announcementInMemoryRepository = announcementInMemoryRepository;

        this.announcementSqlRepository = announcementSqlRepository;
    }

    public List<Announcement> loadAnnouncements() {

        if (!authorizationService.hasPermission(Permission.ANNOUNCE_VIEW)) {
            throw new PermissionException("No tienes permiso para ver anuncios");
        }
        List<Announcement> ann = announcementSqlRepository.getPage(
                staffInMemoryRepository.getCurrent().getUuid(), 0, 5);
        announcementInMemoryRepository.setAll(ann);
        return ann;
    }

    public List<Announcement> getAnnouncements() {

        if (!authorizationService.hasPermission(Permission.ANNOUNCE_VIEW)) {
            throw new PermissionException("No tienes permiso para ver anuncios");
        }
        return announcementInMemoryRepository.getAll();
    }

    public List<Announcement> loadMoreAnnouncements(int countCurrent) {

        if (!authorizationService.hasPermission(Permission.ANNOUNCE_VIEW)) {
            throw new PermissionException("No tienes permiso para ver anuncios");
        }
        List<Announcement> ann = announcementSqlRepository.getPage(
                staffInMemoryRepository.getCurrent().getUuid(), countCurrent, countCurrent +5);

        if (ann.isEmpty()) return ann;

        for (Announcement a: ann){
            announcementInMemoryRepository.addEntity(a);
        }
        return ann;
    }

    private boolean isAuthor(Announcement a) {
        return a.getAuthorUuid() != null && a.getAuthorUuid().equalsIgnoreCase(
                staffInMemoryRepository.getCurrent().getUuid());
    }

    public boolean canEdit(Announcement a) {

        return a != null
                && authorizationService.hasPermission(
                Permission.ANNOUNCE_VIEW
        )
                && authorizationService.hasPermission(
                Permission.ANNOUNCE_EDIT
        );
    }

    public List<AnnouncementComment> getComments(
            Announcement target) {

        if (target == null ||
                !authorizationService.hasPermission(
                        Permission.ANNOUNCE_VIEW)) {

            throw new PermissionException(
                    "No tienes permiso para consultar comentarios."
            );
        }

        return announcementSqlRepository.getComments(
                target.getUuid()
        );
    }


}
