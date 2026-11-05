package org.example.servinet.application.port.out.sql;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.ClientPlan;
import org.example.servinet.domain.entities.client.Order;

import java.util.List;

public interface ClientRepository {

    List<Client> getClientsWithOrders();

    void createClientAndOrder(Client client, Order order);

    List<ClientPlan> getAllPlansDatabase();

    void updateClientDatabase(Client client);

    void deleteClientDatabase(Client client);

    void deletePlanDatabase(String planId);

    void addPlan(ClientPlan plan);

    int getAllConnectedClients();

    int getConnectedClientsByAntenna(String antennaUuid);
}
