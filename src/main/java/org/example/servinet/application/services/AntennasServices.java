package org.example.servinet.application.services;

import org.example.servinet.application.dto.AntennaDto;
import org.example.servinet.domain.entities.antenna.Antenna;
import org.example.servinet.domain.entities.antenna.LogAntenna;
import org.example.servinet.domain.enums.Role;
import org.example.servinet.exception.RoleNoPermission;
import org.example.servinet.utils.ImageConverter;
import org.example.servinet.utils.UuidGenerate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Arrays.stream;

public class AntennasServices {

    private static List<Antenna> listAntennas = new ArrayList<Antenna>();


    public static List<Antenna> getAllAntennas(){
        if (listAntennas.isEmpty()) {
            return null;
        }
        return listAntennas;
    }

    public static Antenna getAntenna(String idAntenna){

        if (idAntenna == null) {
            return null;
        }

        if (listAntennas.isEmpty()) {
            return null;
        }

        Antenna result = listAntennas.stream()
                .filter(antena -> antena.getUuid() == idAntenna)
                .findFirst()
                .orElse(null);

        return result;
    }


    public static void addAntenna(AntennaDto antennaDto) throws RoleNoPermission, IOException {
        Role role = SessionService.getUserRol();
        if (role != Role.ADMIN && role != Role.OWNER){
            throw new RoleNoPermission("No tienes el rol necesario para crear una antena");
        }

        if (antennaDto.getName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")){
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras y espacios."
            );
        }
        String image = "";
        try {
             image = ImageConverter.toBase64(antennaDto.getImage());
        }catch(IOException ex){
            throw new IOException("No se pudo procesar la imagen adjuntada de la antena.");
        }

        Antenna newEntityAntenna = new Antenna(
                UuidGenerate.getNewUuid(),
                antennaDto.getPriority(),
                antennaDto.getName(),
                antennaDto.getSector(),
                image,
                antennaDto.getStatus(),
                antennaDto.getMaintenanceCount(),
                antennaDto.isForReair(),
                antennaDto.getDateLastMaintenance(),
                LocalDateTime.now(),
                antennaDto.getDaysOn()
        );

        listAntennas.add(newEntityAntenna);
    }
}
