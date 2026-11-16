package org.example.servinet.application.usecase.antenna;

import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.application.port.out.sql.SqlComplexRepository;
import org.example.servinet.domain.entities.Antenna;

import java.util.List;

public class GetAntennaUseCase {
    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final SqlComplexRepository<Antenna> sqlAntennaRepository;


    public GetAntennaUseCase(InMemoryRepository<Antenna> inMemoryAntennasRepository, SqlComplexRepository sqlAntennaRepository) {
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
        System.out.println("Se obtuvieron las antenas");
        return antennas;
    }

    public Antenna getAntenna(String uuid){
        List<Antenna> antennas = getAll();

        for (Antenna a: antennas){
            if (a.getUuid().equals(uuid)){
                return a;
            }
        }
        return null;
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
