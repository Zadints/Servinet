package org.example.servinet.application.service;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.RelationAntClinRepository;
import org.example.servinet.application.port.out.sql.ComplexRepository;
import org.example.servinet.domain.entities.Antenna;

import java.util.List;

public class AntennaService {
    private final ComplexRepository<Antenna> sqlAntennaRepository;
    private final InMemoryRepository<Antenna> inMemoryAntennasRepository;
    private final RelationAntClinRepository inMemoryRelationAntClinRepository;

    public AntennaService(ComplexRepository<Antenna> sqlAntennaRepository, InMemoryRepository<Antenna> inMemoryAntennasRepository, RelationAntClinRepository relationAntClinRepository) {
        this.sqlAntennaRepository = sqlAntennaRepository;
        this.inMemoryAntennasRepository = inMemoryAntennasRepository;
        this.inMemoryRelationAntClinRepository = relationAntClinRepository;
    }

    public void addClientCountAntenna(String antennaUuid, String dniClient){
        sqlAntennaRepository.addRelation(antennaUuid,dniClient);
        inMemoryRelationAntClinRepository.addRelation(antennaUuid,dniClient );
    }



}
