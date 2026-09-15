package org.example.servinet.core.application.usecase;

import javafx.scene.image.Image;
import org.example.servinet.core.application.dto.AntennaDto;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.domain.entities.Antenna;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.exception.FileExistException;
import org.example.servinet.core.domain.exception.RoleNoPermission;
import org.example.servinet.core.domain.repository.IdGenerator;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.core.application.service.UuidGenerator;
import org.example.servinet.infrastructure.database.models.AntennaModel;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static java.util.Arrays.stream;

public class AntennasUseCase {

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

    public static void loadAntennas(){
        listAntennas = AntennaModel.getAllAntennaDatabase();
    }

    public static String addAntenna(AntennaDto antennaDto) {

        if (PermissionValidation.hasPermission(Permission.ANT_CREATE)){
            throw new RoleNoPermission("No tienes el rol necesario para crear una antena");
        }

        LocalDate date = antennaDto.getDateLastMaintenance();
        LocalTime time = antennaDto.getTimeLastMaintenance();
        LocalDateTime lastMaintenance = date.atTime(time);;

        if (date != null && time != null) {
            return "Debes ingresar fecha y hora válidas";
        }

        if (antennaDto.getName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")){
            return "El nombre solo puede contener letras y espacios.";
        }

        Image image;

        try {
             image = ImageConverter.toImage(antennaDto.getImage());
        }catch(IOException ex){
            throw new FileExistException(antennaDto.getImage());
        }

        /*
        Antenna newEntityAntenna = new Antenna(
                new GenerateIdUseCase(new UuidGenerator()).execute(),
                antennaDto.getPriority(),
                antennaDto.getName(),
                antennaDto.isForReair(),
                antennaDto.isForMaintenance(),
                antennaDto.getDateLastMaintenance(),
                antennaDto.getCountDaysOn(),
                0,
                image,
                antennaDto.getStatus(),
                LocalDateTime.now()
        );
        AntennaModel.setAntennaDatabase(newEntityAntenna);
        listAntennas.add(newEntityAntenna);
*/      return "";
    }
}
