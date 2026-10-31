package org.example.servinet.application.usecase;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.InMemoryTechnicalRepository;
import org.example.servinet.application.service.ClientService;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.technicians.Technical;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.enums.TechnicalStatus;
import org.example.servinet.domain.exception.DependencyNotLoaded;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


public class TechnicalUseCase {

    private final InMemoryTechnicalRepository inMemoryTechnical;
    private final StaffCacheService staffCacheService;
    private final ClientService clientService;

    public TechnicalUseCase(InMemoryTechnicalRepository inMemoryTechnical,
                            StaffCacheService staffCacheService,
                            ClientService clientService) {
        this.inMemoryTechnical = inMemoryTechnical;
        this.staffCacheService = staffCacheService;
        this.clientService = clientService;
    }

//erro client mas staff deben estar cargados
    public List<Technical> getAll(){
        /*
        System.out.println("1) - Obteniendo los staff");
        List<Technical> technicals = inMemoryTechnical.getAll();

        if (technicals != null && !technicals.isEmpty()){
            return technicals;
        }*/
        inMemoryTechnical.setAll(new ArrayList<>());

        System.out.println("2) - Obteniendo los staff de db");

        List<Staff> staff = staffCacheService.getAllStaff();

        System.out.println(staff.size());


        if (staff.isEmpty()){
            throw new DependencyNotLoaded("Técnicos", "Staff list");
        }

        int technicalExistCount = 0;
        int technicalAvailableCount = 0;
        int technicalBusyCount = 0;
        System.out.println("staff : "+ staff.size());
        for (Staff s : staff){
            System.out.println("entró al for");
            if (isTechnician(s.getRol().getName())){

                String uuidTechnician = s.getUuid() ;

                if (uuidTechnician == null){
                    throw new DependencyNotLoaded("Técnicos", "Staff - uuidTechnician is null");
                }
                System.out.println("paso if");
                technicalExistCount++;

                List<OrderReportDto> ordersBrute = clientService.getOrdersByTechnician(uuidTechnician);

                TechnicalStatus technicianStatus = TechnicalStatus.AVAILABLE;
                technicalAvailableCount++;

                System.out.println("paso if 734272367" + ordersBrute.size());
                if (!ordersBrute.isEmpty()){

                    if (ordersBrute.getFirst().getReportStatus() == OrderStatus.INSTALLING){
                        technicianStatus = TechnicalStatus.BUSY;
                        technicalAvailableCount--;
                        technicalBusyCount++;
                    }
                }
                System.out.println("paso if731o8yyqgdnjskmalñd{asdnakashdjkadads");
                inMemoryTechnical.addTechnical(new Technical(s, technicianStatus));
            }
        }
        System.out.println("preparando return");
        inMemoryTechnical.setTechnicalAlreadyExist(technicalExistCount);
        inMemoryTechnical.setTechnicalAvailable(technicalAvailableCount) ;
        inMemoryTechnical.setTechnicalBusy(technicalBusyCount);
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

    public int getTechnicalAlreadyExist() {
        return inMemoryTechnical.getTechnicalAlreadyExist();
    }

    public int getTechnicalAvailable() {
        return inMemoryTechnical.getTechnicalAvailable();
    }

    public int getTechnicalBusy() {
        return inMemoryTechnical.getTechnicalBusy();
    }
}
