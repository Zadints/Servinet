package org.example.servinet.application.usecase.antenna;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.ComplexRepository;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.LogType;

public class DeleteAntennaUseCase {
    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final ComplexRepository<Antenna> sqlAntennaRepository;
    private final ActivityLogsService activityLogsService;

    public DeleteAntennaUseCase(InMemoryRepository<Antenna> inMemoryAntennasRepository, ComplexRepository<Antenna> sqlAntennaRepository,  ActivityLogsService activityLogsService) {
        this.inMemoryAntennasRepository = inMemoryAntennasRepository;
        this.sqlAntennaRepository = sqlAntennaRepository;
        this.activityLogsService = activityLogsService;
    }

    public void deleteAntenna(Antenna antenna){

        sqlAntennaRepository.delete(antenna.getUuid());
        inMemoryAntennasRepository.remove(antenna.getUuid());

        activityLogsService.addLog(
                LogType.ANT_REMOVE,
                "Se eliminó la antena "+ antenna.getName() +" ubicada en "+ antenna.getLatitude()+ antenna.getLongitude());

    }
}
