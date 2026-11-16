package org.example.servinet.application.usecase.antenna;

import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.application.port.out.sql.SqlComplexRepository;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.antenna.StatusAntenna;

public class MaintenanceAntennaUseCase {

    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final SqlComplexRepository<Antenna> sqlAntennaRepository;

    public MaintenanceAntennaUseCase(InMemoryRepository<Antenna> inMemoryAntennasRepository, SqlComplexRepository<Antenna> sqlAntennaRepository) {
        this.inMemoryAntennasRepository = inMemoryAntennasRepository;
        this.sqlAntennaRepository = sqlAntennaRepository;
    }

    public void on(Antenna antenna){

        if (antenna.isForMaintenance()){
            return;
        }

        antenna.setForMaintenance(true);
        inMemoryAntennasRepository.edit(antenna);
        sqlAntennaRepository.toggleMaintenance(antenna.getUuid());
    }
    public void off(Antenna antenna){

        if (antenna.getStatus().equals(StatusAntenna.ACTIVE) ||antenna.getStatus().equals(StatusAntenna.OFFLINE) ){
            return;
        }

        antenna.setForMaintenance(false);
        antenna.setStatus(StatusAntenna.ACTIVE);
        inMemoryAntennasRepository.edit(antenna);
        sqlAntennaRepository.updateStatus(antenna.getUuid(), StatusAntenna.ACTIVE);
    }


}
