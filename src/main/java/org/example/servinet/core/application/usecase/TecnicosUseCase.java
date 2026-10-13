package org.example.servinet.core.application.usecase;

import org.example.servinet.core.application.dto.OrderReportDto;
import org.example.servinet.core.domain.entities.Staff;
import org.example.servinet.core.domain.entities.technicians.Technician;
import org.example.servinet.core.domain.entities.technicians.Technician;
import org.example.servinet.core.domain.exception.DependencyNotLoaded;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import static org.example.servinet.core.application.usecase.ClientsUseCase.getClientByTechnician;

public class TecnicosUseCase {

    private static List<Technician> allTechniciansList = new ArrayList<>();

    public static void automaticTechniciansLoad(String hardwareId){

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

                OrderReportDto orderBrute = getClientByTechnician(uuidTechnician);

                /*
                allTechniciansList.add(
                        new Technician(
                                s,
                                orderBrute.
                                )
                ); */
            }
        }
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
