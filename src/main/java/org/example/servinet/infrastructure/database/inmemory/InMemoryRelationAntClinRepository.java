package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.port.out.RelationAntClinRepository;

import java.util.*;

public class InMemoryRelationAntClinRepository implements RelationAntClinRepository {

    private final Map<String, Set<String>> relations = new HashMap<>();

    @Override
    public void addRelation(String antennaUuid, String clientDni) {
        relations.computeIfAbsent(antennaUuid, key -> new HashSet<>())
                .add(clientDni);
    }

    @Override
    public List<String> getClientsByAntenna(String antennaUuid) {
        return new ArrayList<>(
                relations.getOrDefault(antennaUuid, Collections.emptySet())
        );
    }

    @Override
    public void removeRelation(String antennaUuid, String clientDni) {
        Set<String> clients = relations.get(antennaUuid);

        if (clients == null) return;

        clients.remove(clientDni);

        if (clients.isEmpty()) {
            relations.remove(antennaUuid);
        }
    }

    @Override
    public void removeRelationsByAntenna(String antennaUuid) {
        relations.remove(antennaUuid);
    }

    @Override
    public void removeRelationsByClient(String clientDni) {
        relations.values().forEach(clients -> clients.remove(clientDni));
        relations.values().removeIf(Set::isEmpty);
    }
}
