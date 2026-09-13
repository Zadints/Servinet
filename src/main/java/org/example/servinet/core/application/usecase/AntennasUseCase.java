package org.example.servinet.core.application.usecase;

import javafx.scene.image.Image;
import org.example.servinet.core.application.dto.AntennaDto;
import org.example.servinet.core.domain.entities.Antenna;
import org.example.servinet.core.domain.enums.Role;
import org.example.servinet.core.domain.exception.RoleNoPermission;
import org.example.servinet.core.domain.utils.ImageConverter;
import org.example.servinet.core.domain.utils.UuidGenerate;
import org.example.servinet.infrastructure.database.models.AntennaModel;

import java.io.IOException;
import java.time.LocalDateTime;
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

    public static void addAntenna(AntennaDto antennaDto) throws RoleNoPermission, IOException {
        Role role = SessionUseCase.getUserRol();
        if (role != Role.ADMIN && role != Role.OWNER){
            throw new RoleNoPermission("No tienes el rol necesario para crear una antena");
        }

        if (antennaDto.getName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")){
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras y espacios."
            );
        }
        Image image;
        try {
             image = ImageConverter.toImage(antennaDto.getImage());

        }catch(IOException ex){
            throw new IOException("No se pudo procesar la imagen adjuntada de la antena.");
        }

        Antenna newEntityAntenna = new Antenna(
                UuidGenerate.getNewUuid(),
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
        //argegar logs aqui
    }
}
