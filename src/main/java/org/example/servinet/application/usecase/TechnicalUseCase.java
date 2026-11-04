package org.example.servinet.application.usecase;

import org.example.servinet.application.dto.OrderContainer;
import org.example.servinet.application.port.out.InMemoryTechnicalRepository;
import org.example.servinet.application.port.out.sql.TechnicalRepository;
import org.example.servinet.application.service.ClientService;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.technicians.Technical;
import org.example.servinet.domain.exception.DependencyNotLoaded;
import org.example.servinet.infrastructure.dto.TechnicalStatic;

import java.text.Normalizer;
import java.util.List;


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

//erro client mas staff deben estar cargados
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

    public int getTechnicalAlreadyExist() {
        return inMemoryTechnical.getTechnicalAlreadyExist();
    }

    public int getTechnicalAvailable() {
        return inMemoryTechnical.getTechnicalAvailable();
    }

    public int getTechnicalBusy() {
        return inMemoryTechnical.getTechnicalAllInstalled();
    }
}
