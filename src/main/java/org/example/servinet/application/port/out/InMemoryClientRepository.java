package org.example.servinet.application.port.out;

import org.example.servinet.domain.entities.client.Client;

import java.util.List;
import java.util.UUID;

public interface InMemoryClientRepository {

    List<Client> getAllClients();
    void addClient(Client entity);

    Client getClient(String dni);
    void removeClient(Client client);
    void editClient(Client entity);
    int getConnectedClientsByAntenna(UUID antennaId);
    int getAllConnectedClients();
}
