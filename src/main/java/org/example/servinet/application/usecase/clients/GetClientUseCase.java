package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.application.service.PlanService;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.ClientPlan;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.infrastructure.database.sqlserver.SqlClientRepository;

import java.util.*;

public class GetClientUseCase {
    private final InMemoryClientRepository inMemoryClientRepository;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;
    private final PlanService planService;
    private final AuthorizationSecurity authorizationSecurity;
    public GetClientUseCase(InMemoryClientRepository inMemoryClientRepository, ClientRepository sqlClientAuthRepository, PaymentsService paymentsService, PlanService planService, AuthorizationSecurity authorizationSecurity) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.paymentsService = paymentsService;
        this.planService = planService;
        this.authorizationSecurity = authorizationSecurity;
    }

    public int getConnectedClientsAntenna(String uuid) {
        ensureClientsLoaded();

        UUID antennaUuid = UUID.fromString(uuid);

        return inMemoryClientRepository.getConnectedClientsByAntenna(antennaUuid);
    }

    public int getAllClientConectedAntenna() {
        ensureClientsLoaded();

        return inMemoryClientRepository.getAllConnectedClients();
    }

    private void ensureClientsLoaded() {
        if (inMemoryClientRepository.getAllClients().isEmpty()) {
            getAll();
        }
    }

    public List<Client> getAll() {

        List<Client> cachedClients = inMemoryClientRepository.getAllClients();

        if (cachedClients != null && !cachedClients.isEmpty()) {
            return cachedClients;
        }

        List<OrderReportDto> orders =
                sqlClientAuthRepository.getAllClientsOrderDatabase();

        Map<String, Client> clientsMap = new LinkedHashMap<>();

        for (OrderReportDto o : orders) {
            inMemoryClientRepository.addOrder(o);
            // Evitar clientes duplicados por sus múltiples pedidos/imágenes.
            if (clientsMap.containsKey(o.getClientDni())) {
                continue;
            }

            Client client = new Client(
                    o.getClientDni(),
                    o.getClientEmail(),
                    o.getClientName(),
                    o.getClientLastName(),
                    o.getClientPhone(),
                    o.getClientWhatsapp(),
                    o.getClientAddress(),
                    null,
                    o.getOrderStartInstall(),
                    o.getOrderCreate(),
                    o.getClientStatus(),
                    o.getPaymentStatus(),
                    o.getImgRouter(),
                    null
            );

            ClientPlan plan = new ClientPlan(
                    o.getPlanId(),
                    o.getPlanName(),
                    o.getPlanSpeed(),
                    o.getPlanPrice(),
                    o.getClientStatus() == ClientStatus.ACTIVO
            );

            client.setPlan(plan);

            clientsMap.put(client.getDni(), client);
            inMemoryClientRepository.addClient(client);

        }

        return new ArrayList<>(clientsMap.values());
    }


    public Client getClient(String dni) {
        if (dni == null || inMemoryClientRepository.getAllClients().isEmpty()) {
            return null;
        }
        return inMemoryClientRepository.getClient(dni);
    }

    public int getTotalClients() {
        return inMemoryClientRepository.getAllClients().size();
    }

    public long getClientsUpToDate() {
        return inMemoryClientRepository.
                getAllClients().stream().filter(
                        c -> c.getPlanStatus() == PlanStatusClient.AL_DIA).count();
    }

    public long getClientsPending() {
        return inMemoryClientRepository.getAllClients().stream()
                .filter(c -> c.getPlanStatus() == PlanStatusClient.CON_DEUDA).count();
    }

    public Client getBestClient() {
        return inMemoryClientRepository.getAllClients().stream()
                .max(Comparator.comparingInt(Client::getScorePayment))
                .orElse(null);
    }

    public List<Client> getMostIrresponsibleClients() {
        return inMemoryClientRepository.getAllClients().stream()
                .sorted(Comparator.comparingInt(Client::getScorePayment))
                .toList();
    }

    public List<Client> searchClients(String searchType, String query) {
        if (!authorizationSecurity.hasPermission(Permission.CLIENT_SEARCH)
                && !authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException("No tienes el rol necesario para buscar clientes");
        }

        if (query == null || query.isBlank()) {
            return getAll();
        }

        String normalized = query.trim();

        List<OrderReportDto> orders =
                sqlClientAuthRepository.searchClients(searchType.toUpperCase(), normalized);

        List<Client> clients = new ArrayList<>();

        for (OrderReportDto o : orders) {
            Client client = new Client(
                    o.getClientDni(),
                    o.getClientEmail(),
                    o.getClientName(),
                    o.getClientLastName(),
                    o.getClientPhone(),
                    o.getClientWhatsapp(),
                    o.getClientAddress(),
                    null,
                    o.getOrderStartInstall(),
                    o.getOrderCreate(),
                    o.getClientStatus(),
                    o.getPaymentStatus(),
                    o.getImgRouter(),
                    null
            );

            ClientPlan plan = new ClientPlan(
                    o.getPlanId(),
                    o.getPlanName(),
                    o.getPlanSpeed(),
                    o.getPlanPrice(),
                    o.getClientStatus() == ClientStatus.ACTIVO
            );

            client.setPlan(plan);
            clients.add(client);
        }

        return clients;
    }

    private boolean matchesSearch(Client c, String searchType, String query) {
        if (searchType == null) {
            return matchesAnyField(c, query);
        }
        return switch (searchType) {
            case "Persona" -> (c.getFullName() != null && c.getFullName().toLowerCase().contains(query))
                    || (c.getDni() != null && c.getDni().toLowerCase().contains(query))
                    || (c.getPhone() != null && c.getPhone().toLowerCase().contains(query));
            case "Dirección" -> c.getAddress() != null && c.getAddress().toLowerCase().contains(query);
            case "Plan" -> c.getPlan() != null && c.getPlan().getName() != null
                    && c.getPlan().getName().toLowerCase().contains(query);
            default -> matchesAnyField(c, query);
        };
    }
    private boolean matchesAnyField(Client c, String query) {
        return (c.getFullName() != null && c.getFullName().toLowerCase().contains(query))
                || (c.getDni() != null && c.getDni().toLowerCase().contains(query))
                || (c.getPhone() != null && c.getPhone().toLowerCase().contains(query))
                || (c.getAddress() != null && c.getAddress().toLowerCase().contains(query));
    }

}
