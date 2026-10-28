package org.example.servinet.application.usecase;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.port.out.InMemoryComplexRepository;
import org.example.servinet.application.port.out.InMemoryTechnicalRepository;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.technicians.Technical;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.enums.TechnicalStatus;
import org.example.servinet.domain.exception.DependencyNotLoaded;
import org.example.servinet.infrastructure.database.inmemory.InMemoryStaffAuthRepository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


import static org.example.servinet.application.usecase.ClientsUseCase.getOrdersByTechnician;

public class TechnicalUseCase {

    private final InMemoryTechnicalRepository inMemoryTechnical;
    private final InMemoryComplexRepository<Staff> inMemoryStaffRepository;

    public TechnicalUseCase(InMemoryTechnicalRepository inMemoryTechnical, InMemoryComplexRepository<Staff> inMemoryStaffRepository) {
        this.inMemoryTechnical = inMemoryTechnical;
        this.inMemoryStaffRepository = inMemoryStaffRepository;
    }


    public List<Technical> getAll(){

        List<Technical> technicals = inMemoryTechnical.getAll();
        if (technicals != null){
            return technicals;
        }

        System.out.println("1) - Obteniendo los staff");

        List<Staff> staff = inMemoryStaffRepository.getAll();

        if (staff == null){
            throw new DependencyNotLoaded("Técnicos", "Staff list");
        }

        int technicalExistCount = 0;
        int technicalAvailableCount = 0;
        int technicalBusyCount = 0;

        for (Staff s : staff){
            if (isTechnician(s.getRol().getName())){

                String uuidTechnician = s.getUuid() ;

                if (uuidTechnician == null){
                    throw new DependencyNotLoaded("Técnicos", "Staff - uuidTechnician is null");
                }

                technicalExistCount++;
                /*Ojo tenemos valores de reportCreate desde la más reciente a la más antigua*/
                List<OrderReportDto> ordersBrute = getOrdersByTechnician(uuidTechnician);

                TechnicalStatus technicianStatus = TechnicalStatus.AVAILABLE;
                technicalAvailableCount++;
                if (!ordersBrute.isEmpty()){

                    if (ordersBrute.getFirst().getReportStatus() == OrderStatus.INSTALLING){
                        technicianStatus = TechnicalStatus.BUSY;
                        technicalAvailableCount--;
                        technicalBusyCount++;
                    }
                }

                inMemoryTechnical.addTechnical(new Technical(s, technicianStatus));
            }
        }

        inMemoryTechnical.setTechnicalAlreadyExist(technicalExistCount);
        inMemoryTechnical.setTechnicalAvailable(technicalAvailableCount) ;
        inMemoryTechnical.setTechnicalBusy(technicalBusyCount);

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
