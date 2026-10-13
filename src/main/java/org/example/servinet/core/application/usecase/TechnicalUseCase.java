package org.example.servinet.core.application.usecase;

import org.example.servinet.core.application.dto.OrderReportDto;
import org.example.servinet.core.domain.entities.Staff;
import org.example.servinet.core.domain.entities.technicians.Technical;
import org.example.servinet.core.domain.enums.OrderStatus;
import org.example.servinet.core.domain.enums.TechnicalStatus;
import org.example.servinet.core.domain.exception.DependencyNotLoaded;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


import static org.example.servinet.core.application.usecase.ClientsUseCase.getOrdersByTechnician;

public class TechnicalUseCase {

    private static List<Technical> allTechniciansList = new ArrayList<>();

    public static void automaticTechniciansLoad(){

        List<Staff> staff = SessionUseCase.getAllStaffList();
        System.out.println("1) - Obteniendo los staff");
        if (staff == null){
            throw new DependencyNotLoaded("Técnicos", "Staff");
        }


        for (Staff s : staff){
            if (isTechnician(s.getRol().getName())){

                String uuidTechnician = s.getUuid() ;
                System.out.println("Uuid staff: " + uuidTechnician);
                if (uuidTechnician == null){
                    throw new DependencyNotLoaded("Técnicos", "Staff - uuidTechnician is null");
                }
                /*Ojo tenemos valores de reportCreate desde la más reciente a la más antigua*/
                List<OrderReportDto> ordersBrute = getOrdersByTechnician(uuidTechnician);

                TechnicalStatus technicianStatus = TechnicalStatus.AVAILABLE;
                if (!ordersBrute.isEmpty()){

                    if (ordersBrute.getFirst().getReportStatus() == OrderStatus.INSTALLING){
                        technicianStatus = TechnicalStatus.BUSY;
                    }
                }
                allTechniciansList.add(new Technical(s, technicianStatus));
            }
        }
    }



    public static List<Technical> getTechnicalsAll(){
        return allTechniciansList;
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
}
