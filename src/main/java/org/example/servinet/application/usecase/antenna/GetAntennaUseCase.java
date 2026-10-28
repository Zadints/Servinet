package org.example.servinet.application.usecase.antenna;

import javafx.concurrent.Task;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.SqlComplexRepository;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.infrastructure.database.inmemory.InMemoryAntennasRepository;

import java.util.List;

public class GetAntennaUseCase {
    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final SqlComplexRepository<Antenna> sqlAntennaRepository;


    public GetAntennaUseCase(InMemoryRepository<Antenna> inMemoryAntennasRepository, SqlComplexRepository sqlAntennaRepository, AppExecutor executor) {
        this.inMemoryAntennasRepository = inMemoryAntennasRepository;
        this.sqlAntennaRepository = sqlAntennaRepository;
    }

    public List<Antenna> getAll(){

        List<Antenna> antennas = inMemoryAntennasRepository.getAll();

        if (!antennas.isEmpty()){
            return antennas;
        }

        antennas = sqlAntennaRepository.getAll();

        if (antennas.isEmpty()) return null;

        inMemoryAntennasRepository.setAll(antennas);
        return antennas;
    }


    public byte[] getImageMaintenance(String id){

        if (id == null) return null;

        byte[] image = sqlAntennaRepository.getImage(id);

        if (image == null) {
            return null;
        }
        return image;
    }
}
