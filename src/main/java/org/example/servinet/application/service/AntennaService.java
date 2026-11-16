package org.example.servinet.application.service;

import org.example.servinet.application.port.out.inmemory.InMemoryRepository;
import org.example.servinet.application.port.out.inmemory.InMemoryRelationAntennaRepository;
import org.example.servinet.application.port.out.sql.SqlComplexRepository;
import org.example.servinet.domain.entities.Antenna;

public class AntennaService {
    private final SqlComplexRepository<Antenna> sqlAntennaRepository;
    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final InMemoryRelationAntennaRepository inMemoryInMemoryRelationAntennaRepository;

    public AntennaService(SqlComplexRepository<Antenna> sqlAntennaRepository, InMemoryRepository<Antenna> inMemoryAntennasRepository, InMemoryRelationAntennaRepository inMemoryRelationAntennaRepository) {
        this.sqlAntennaRepository = sqlAntennaRepository;
        this.inMemoryAntennasRepository = inMemoryAntennasRepository;
        this.inMemoryInMemoryRelationAntennaRepository = inMemoryRelationAntennaRepository;
    }

    public void addClientCountAntenna(String antennaUuid, String dniClient){
        sqlAntennaRepository.addRelation(antennaUuid,dniClient);
        inMemoryInMemoryRelationAntennaRepository.addRelation(antennaUuid,dniClient );
    }



}
