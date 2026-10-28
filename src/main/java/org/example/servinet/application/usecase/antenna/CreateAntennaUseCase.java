package org.example.servinet.application.usecase.antenna;

import javafx.scene.image.Image;
import org.example.servinet.application.dto.AntennaDto;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlComplexRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.FileExistException;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.ui.ImageConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class CreateAntennaUseCase {
    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final SqlComplexRepository<Antenna> sqlAntennaRepository;


    public CreateAntennaUseCase(InMemoryRepository<Antenna> inMemoryAntennasRepository, SqlComplexRepository<Antenna> sqlAntennaRepository, AppExecutor executor) {
        this.inMemoryAntennasRepository = inMemoryAntennasRepository;
        this.sqlAntennaRepository = sqlAntennaRepository;
    }

    public static String create(AntennaDto antennaDto) {

        LocalDate date = antennaDto.getDateLastMaintenance();
        LocalTime time = antennaDto.getTimeLastMaintenance();

        LocalDateTime lastMaintenance = date.atTime(time);

        if (antennaDto.getName() == null
                || !antennaDto.getName().matches(
                "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+"
        )) {

            return "El nombre solo puede contener letras y espacios.";
        }

        /*
        Antenna newEntityAntenna = new Antenna(
                UUID.randomUUID().toString(),
                antennaDto.getPriority().,
                antennaDto.getName(),
                antennaDto.isForReair(),
                antennaDto.isForMaintenance(),
                antennaDto.getDateLastMaintenance(),
                antennaDto.getCountDaysOn(),
                0,
                antennaDto.getImage(),
                antennaDto.getStatus(),
                LocalDateTime.now()
        );
        AntennaModel.setAntennaDatabase(newEntityAntenna);
        listAntennas.add(newEntityAntenna);*/
        return "";
    }
}
