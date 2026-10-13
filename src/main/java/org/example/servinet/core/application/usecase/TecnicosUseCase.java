package org.example.servinet.core.application.usecase;

import org.example.servinet.core.application.dto.OrderReportDto;
import org.example.servinet.core.domain.entities.Staff;
import org.example.servinet.core.domain.entities.technicians.Order;
import org.example.servinet.core.domain.entities.technicians.Technician;
import org.example.servinet.core.domain.entities.technicians.Technician;
import org.example.servinet.core.domain.enums.TechnicianStatus;
import org.example.servinet.core.domain.exception.DependencyNotLoaded;
import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import static org.example.servinet.core.application.usecase.ClientsUseCase.getOrdersByTechnician;

public class TecnicosUseCase {

    private static List<Technician> allTechniciansList = new ArrayList<>();

    public static void automaticTechniciansLoad(){

        List<Staff> staff = SessionUseCase.getAllStaffList();

        if (staff == null){
            throw new DependencyNotLoaded("Técnicos", "Staff");
        }


        for (Staff s : staff){
            if (isTechnician(s.getRol().getName())){

                String uuidTechnician = s.getUuid() ;

                if (uuidTechnician == null){
                    throw new DependencyNotLoaded("Técnicos", "Staff - uuidTechnician is null");
                }

                List<OrderReportDto> ordersBrute = getOrdersByTechnician(uuidTechnician);



                //TechnicianStatus technicianStatus = getTechnicianStatus(orderBrute);


                //allTechniciansList.add(new Technician(s, technicianStatus));
            }
        }
    }

    @NotNull
    private static TechnicianStatus getTechnicianStatus(OrderReportDto orderBrute) {
        TechnicianStatus technicianStatus = TechnicianStatus.AVAILABLE;

        if (orderBrute != null){
            LocalDateTime startInstall = orderBrute.getOrderStartInstall();
            if (startInstall != null){
                LocalDateTime endInstall = orderBrute.getOrderEndInstall();
                if (endInstall == null){
                    technicianStatus = TechnicianStatus.BUSY;
                }
            }
        }
        return technicianStatus;
    }


    public static List<Technician> getTechniciansAll(){
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
