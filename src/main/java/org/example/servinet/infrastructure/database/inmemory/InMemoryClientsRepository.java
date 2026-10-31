
package org.example.servinet.infrastructure.database.inmemory;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.domain.entities.Client;

import java.util.*;

public class InMemoryClientsRepository implements InMemoryClientRepository {

    private final Map<String, Client> clients = new HashMap<>();
    private final List<OrderReportDto> listOrdersClients = new ArrayList<>();
    private final Map<UUID, Set<String>> antennaConnections = new HashMap<>();

    @Override
    public List<Client> getAllClients() {
        return new ArrayList<>(clients.values());
    }

    @Override
    public List<OrderReportDto> getOrdersByTechnician(String uuidTechnician) {
        return listOrdersClients.stream()
                .filter(Objects::nonNull)
                .filter(order -> order.getTechnicianId() != null)
                .filter(order -> order.getTechnicianId()
                        .toString().equals(uuidTechnician))
                .sorted(Comparator.comparing(
                        OrderReportDto::getReportCreate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Override
    public void addClient(Client entity) {
        Objects.requireNonNull(entity, "El cliente no puede ser null");
        clients.put(entity.getDni(), entity);
    }

    @Override
    public void addOrder(OrderReportDto entity) {
        Objects.requireNonNull(entity, "El reporte no puede ser null");

        listOrdersClients.add(entity);

        UUID antennaId = entity.getReportAntennaConnect();
        String clientDni = entity.getClientDni();

        if (antennaId != null && clientDni != null) {
            antennaConnections
                    .computeIfAbsent(antennaId, id -> new HashSet<>())
                    .add(clientDni);
        }
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
    public int getConnectedClientsByAntenna(UUID antennaUuid) {
        if (antennaUuid == null) return 0;

        return antennaConnections
                .getOrDefault(antennaUuid, Collections.emptySet())
                .size();
    }

    @Override
    public int getAllConnectedClients() {
        return antennaConnections.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    @Override
    public void reset(OrderReportDto entity) {
        clients.clear();
        listOrdersClients.clear();
        antennaConnections.clear();
    }
}
