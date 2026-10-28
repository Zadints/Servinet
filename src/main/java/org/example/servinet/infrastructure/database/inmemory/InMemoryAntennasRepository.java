package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.entities.Role;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryAntennasRepository implements InMemoryRepository<Antenna> {

    /**
     *  map<"UUID antrnna, entity Antenna ">
     **/
    private Map<String, Antenna> antennas = new LinkedHashMap<>();

    @Override
    public List<Antenna> getAll() {
        return new ArrayList<>(antennas.values());
    }
    @Override
    public void setAll(List<Antenna> listEntity) {
        for (Antenna a: listEntity){
            antennas.put(a.getUuid(), a);
        }
    }

    @Override
    public void addEntity(Antenna entity) {
        antennas.put(entity.getUuid(), entity);
    }

    @Override
    public void remove(String id) {
        antennas.remove(id);
    }

    @Override
    public void edit(Antenna entity) {
        antennas.put(entity.getUuid(), entity);
    }

}
