package org.example.servinet.application.usecase.antenna;

import net.bytebuddy.implementation.bytecode.Throw;
import org.example.servinet.application.dto.EditAntennaDto;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.ComplexRepository;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.LogType;

public class EditAntennaUseCase {


    private final ComplexRepository<Antenna> sqlAntennaRepository;
    private final ActivityLogsService activityLogsService;

    public EditAntennaUseCase(ComplexRepository<Antenna> sqlAntennaRepository, ActivityLogsService activityLogsService) {

        this.sqlAntennaRepository = sqlAntennaRepository;
        this.activityLogsService = activityLogsService;
    }

    public void edit(EditAntennaDto dto, Antenna ant){

        if (!dto.name().isEmpty()){
            ant.setName(dto.name());
        }

        if (dto.latitude() != null){
            ant.setLatitude(dto.latitude());
        }
        if (dto.longitude() != null){
            ant.setLongitude(dto.longitude());
        }
        if (dto.image() != null){
            ant.setImage(dto.image());
        }


        sqlAntennaRepository.update(ant);
        activityLogsService.addLog(
                LogType.ANT_CREATE,
                "Editó la antena " + dto.name()

        );
    }
}
