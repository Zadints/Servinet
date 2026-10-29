package org.example.servinet.application.usecase.antenna;

import org.example.servinet.application.dto.AntennaDto;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.ComplexRepository;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Antenna;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CreateAntennaUseCase {
    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final ComplexRepository<Antenna> sqlAntennaRepository;


    public CreateAntennaUseCase(InMemoryRepository<Antenna> inMemoryAntennasRepository, ComplexRepository<Antenna> sqlAntennaRepository, AppExecutor executor) {
        this.inMemoryAntennasRepository = inMemoryAntennasRepository;
        this.sqlAntennaRepository = sqlAntennaRepository;
    }

    public String create(AntennaDto antennaDto) {

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
