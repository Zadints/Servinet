
package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.enums.ClientStatus;

import java.util.*;

public class InMemoryClientsRepository implements InMemoryClientRepository {

    private final Map<String, Client> clients = new HashMap<>();
    private final Map<UUID, Set<String>> antennaConnections = new HashMap<>();

    @Override
    public List<Client> getAllClients() {
        return new ArrayList<>(clients.values());
    }


    @Override
    public void addClient(Client entity) {
        Objects.requireNonNull(entity, "El cliente no puede ser null");
        clients.put(entity.getDni(), entity);
    }



    @Override
    public Client getClient(String dni) {
        return clients.get(dni);
    }

    /**
     * Conserva el cliente y actualiza su estado.
     * El objeto recibido debe tener el estado actualizado.
     */
    @Override
    public void removeClient(Client client) {
        Objects.requireNonNull(client, "El cliente no puede ser null");
        clients.put(client.getDni(), client);
    }

    @Override
    public void editClient(Client entity) {
        Objects.requireNonNull(entity, "El cliente no puede ser null");

        if (!clients.containsKey(entity.getDni())) {
            return;
        }

        clients.put(entity.getDni(), entity);
    }

    @Override
    public int getAllConnectedClients() {
        return antennaConnections.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    @Override
    public int getConnectedClientsByAntenna(UUID antennaUuid) {

        if (antennaUuid == null) {
            return 0;
        }

        return (int) clients.values().stream()
                .filter(Objects::nonNull)
                .filter(client -> client.getOrder() != null)
                .filter(client -> client.getOrder().getAntennaUuid() != null)
                .filter(client -> antennaUuid.toString()
                        .equalsIgnoreCase(client.getOrder().getAntennaUuid()))
                .filter(client -> client.getClientStatus() == ClientStatus.ACTIVO)
                .count();
    }
}
