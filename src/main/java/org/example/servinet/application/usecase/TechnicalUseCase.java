package org.example.servinet.application.usecase;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.usecase.staff.LoginStaffUseCase;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.entities.technicians.Technical;
import org.example.servinet.domain.enums.OrderStatus;
import org.example.servinet.domain.enums.TechnicalStatus;
import org.example.servinet.domain.exception.DependencyNotLoaded;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


import static org.example.servinet.application.usecase.ClientsUseCase.getOrdersByTechnician;

public class TechnicalUseCase {

    private static List<Technical> allTechniciansList = new ArrayList<>();
    private static int technicalAlreadyExist;
    private static int technicalAvailable;
    private static int technicalBusy;


    public static void automaticTechniciansLoad(){

        List<Staff> staff = LoginStaffUseCase.getAllStaff();
        System.out.println("1) - Obteniendo los staff");
        if (staff == null){
            throw new DependencyNotLoaded("Técnicos", "Staff");
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

                allTechniciansList.add(new Technical(s, technicianStatus));
            }
        }

        technicalAlreadyExist = technicalExistCount;
        technicalAvailable = technicalAvailableCount;
        technicalBusy = technicalBusyCount;
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

    public static int getTechnicalAlreadyExist() {
        return technicalAlreadyExist;
    }

    public static int getTechnicalAvailable() {
        return technicalAvailable;
    }

    public static int getTechnicalBusy() {
        return technicalBusy;
    }
}
