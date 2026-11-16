package org.example.servinet.application.port.out.inmemory;

import java.util.List;

public interface InMemoryRelationAntennaRepository {
    void addRelation(String antennaUuid, String clientDni);

    List<String> getClientsByAntenna(String antennaUuid);

    void removeRelation(String antennaUuid, String clientDni);

    void removeRelationsByAntenna(String antennaUuid);

    void removeRelationsByClient(String clientDni);
}
