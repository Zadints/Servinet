package org.example.servinet.application.port.out;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.entities.Client;

import java.util.List;
import java.util.UUID;

public interface InMemoryClientRepository {

    List<Client> getAllClients();
    List<OrderReportDto> getOrdersByTechnician(String uuidTechnician);
    void addClient(Client entity);
    void addOrder(OrderReportDto entity);
    Client getClient(String dni);
    void removeClient(Client client);
    void editClient(Client entity);
    void reset(OrderReportDto entity);
    int getConnectedClientsByAntenna(UUID antennaId);
    int getAllConnectedClients();
}
