package org.example.servinet.application.usecase.antenna;

import org.example.servinet.application.dto.AntennaDto;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.ComplexRepository;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.LogType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class CreateAntennaUseCase {
    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final ComplexRepository<Antenna> sqlAntennaRepository;
    private final ActivityLogsService activityLogsService;


    public CreateAntennaUseCase(InMemoryRepository<Antenna> inMemoryAntennasRepository, ComplexRepository<Antenna> sqlAntennaRepository, ActivityLogsService activityLogsService) {
        this.inMemoryAntennasRepository = inMemoryAntennasRepository;
        this.sqlAntennaRepository = sqlAntennaRepository;
        this.activityLogsService = activityLogsService;
    }

    public void create(AntennaDto dto) {

        LocalDate date = dto.getDateLastMaintenance();
        LocalTime time = dto.getTimeLastMaintenance();

        LocalDateTime lastMaintenance = date.atTime(time);

        if (dto.getName() == null
                || !dto.getName().matches(
                "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+"
        )) {

            throw new IllegalArgumentException("El nombre solo puede contener letras y espacios.") ;
        }

        Antenna newEntityAntenna = new Antenna(
                UUID.randomUUID().toString(),
                dto.getPriority(),
                dto.getName(),
                null,
                dto.isForReair(),
                dto.isForMaintenance(),
                LocalDateTime.now(),
                lastMaintenance,
                dto.getCountDaysOn(),
                0,
                dto.getImage(),
                dto.getStatus(),
                dto.getLatitude(),
                dto.getLongitude()
        );
        inMemoryAntennasRepository.addEntity(newEntityAntenna);
        sqlAntennaRepository.add(newEntityAntenna);
        activityLogsService.addLog(
                LogType.ANT_CREATE,
                "Creaó una nueva antena llamada " + dto.getName() +
                " en las coordenadas ["+ dto.getLatitude() + ", " + dto.getLongitude() + "]"


        );

    }
}
