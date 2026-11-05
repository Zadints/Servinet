package org.example.servinet.application.usecase;

import net.bytebuddy.implementation.bytecode.Throw;
import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.application.port.out.InMemoryTechnicalRepository;
import org.example.servinet.application.port.out.sql.TechnicalRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ClientService;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.domain.entities.InstallationReport;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.technicians.Technical;
import org.example.servinet.domain.entities.technicians.TechnicalLogs;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.DependencyNotLoaded;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.infrastructure.dto.TechnicalStatic;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class TechnicalUseCase {

    private final InMemoryTechnicalRepository inMemoryTechnical;
    private final StaffCacheService staffCacheService;
    private final ClientService clientService;
    private final TechnicalRepository sqlTechnicalRepository;


    public TechnicalUseCase(InMemoryTechnicalRepository inMemoryTechnical,
                            StaffCacheService staffCacheService,
                            ClientService clientService, TechnicalRepository sqlTechnicalRepository) {
        this.inMemoryTechnical = inMemoryTechnical;
        this.staffCacheService = staffCacheService;
        this.clientService = clientService;
        this.sqlTechnicalRepository = sqlTechnicalRepository;
    }


    public List<Technical> getAllTechnicals(){

        List<Technical> technicals = inMemoryTechnical.getAll();

        if (technicals != null && !technicals.isEmpty()){
            return technicals;
        }

        List<Staff> staff = staffCacheService.getAllStaff();

        if (staff.isEmpty()){
            throw new DependencyNotLoaded("Técnicos", "Staff list");
        }

        List<TechnicalStatic> statics = sqlTechnicalRepository.getTechnicianStatistics();

        int technicalExistCount = 0;
        int technicalAvailableCount = 0;
        int installedAllCount = 0;
        System.out.println("staff : "+ staff.size());

        for (Staff s : staff){
            if (isTechnician(s.getRol().getName())){

                int count = 0;
                for (TechnicalStatic t: statics){
                    if (t.staffUuid().equals(s.getUuid())){
                        count = t.allInstalledCount();
                        installedAllCount += count;
                    }
                }


                Technical technical = new Technical(
                        s,
                        count
                );

                inMemoryTechnical.addTechnical(technical);

                technicalExistCount++;
                technicalAvailableCount++;
            }


        }
        System.out.println("preparando return");
        inMemoryTechnical.setTechnicalAlreadyExist(technicalExistCount);
        inMemoryTechnical.setTechnicalAvailable(technicalAvailableCount) ;
        inMemoryTechnical.setTechnicalAllInstalled(installedAllCount);
        System.out.println("Se obtuvieron los staff");
        return inMemoryTechnical.getAll();
    }



    private static boolean isTechnician(String rol) {
        if (rol == null) return false;

        String normalizado = Normalizer.normalize(
                        rol, Normalizer.Form.NFD
                ).replaceAll("\\p{M}", "")
                .replace("_", "")
                .toLowerCase();

        return normalizado.contains("tecnico");
    }
    public List<OrderContainer> getOrderContainer(String uuidStaff){
       return sqlTechnicalRepository.getTechnicianLogs(uuidStaff);
    }

    public List<OrderContainer> getTechnicianLogsByClient(String clientDni){
        return  sqlTechnicalRepository.getTechnicianLogsByClient(clientDni);
    }

    public int getTechnicalAlreadyExist() {
        return inMemoryTechnical.getTechnicalAlreadyExist();
    }

    public int getTechnicalAvailable() {
        return inMemoryTechnical.getTechnicalAvailable();
    }

    public int getTechnicalBusy() {
        return inMemoryTechnical.getTechnicalAllInstalled();
    }


    public TechnicalLogs getCurrentInstallation(){

        String staffUuid = staffCacheService.getCurrent().getUuid();
        Optional<TechnicalLogs> active =
                sqlTechnicalRepository.getActiveInstallation(staffUuid);
        return active.orElse(null);
    }

    public void acceptInstallation(String orderId, String clientDni){
        Staff staff = staffCacheService.getCurrent();

        Optional<TechnicalLogs> active =
                sqlTechnicalRepository.getActiveInstallation(staff.getUuid());

        if (active.isPresent()) {
            throw new InvalidValueException(
                    "Ya tienes una instalación activa. Debes finalizarla primero."
            );
        }

        if (isInstallationProgress(orderId)) {
            throw new InvalidValueException(
                    "Esta instalación ya está siendo realizada por otro técnico."
            );
        }

        sqlTechnicalRepository.acceptInstallation(
                staff.getUuid(),
                orderId,
                clientDni
        );
    }

    public boolean isInstallationProgress(String orderId){
        return sqlTechnicalRepository.isInstallationInProgress(orderId);
    }

    public void finishInstallation( InstallationReport report){
        Staff staff = staffCacheService.getCurrent();

        if (report == null) {
            throw new InvalidValueException("El reporte no puede ser nulo.");
        }

        if (report.getOrderId() == null || report.getOrderId().isBlank()) {
            throw new InvalidValueException("El pedido es obligatorio.");
        }

        try {
            UUID.fromString(report.getOrderId());
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("El UUID del pedido no es válido.");
        }

        if (report.getReportClientDni() == null
                || !report.getReportClientDni().matches("\\d{8}")) {
            throw new InvalidValueException("El DNI debe contener 8 dígitos.");
        }

        if (report.getImgRouter() == null) {
            throw new InvalidValueException(
                    "La imagen del router es obligatoria."
            );
        }

        if (report.getRouterPassword().length() > 255) {
            throw new InvalidValueException(
                    "La contraseña del router no puede superar los 255 caracteres."
            );
        }

        sqlTechnicalRepository.finishInstallation( staff.getUuid(), report );
    }

    public void rejectInstallation(String orderId) {

        String staffUuid = staffCacheService.getCurrent().getUuid();

        try {
            UUID.fromString(staffUuid);
            UUID.fromString(orderId);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("Los UUID no son válidos.");
        }

        sqlTechnicalRepository.rejectInstallation(staffUuid, orderId);
    }
}
