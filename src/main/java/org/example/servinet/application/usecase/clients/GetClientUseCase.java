package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ClientService;
import org.example.servinet.application.service.PlanService;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;

import java.util.*;

public class GetClientUseCase {
    private final InMemoryClientRepository inMemoryClientRepository;
    private final ClientRepository sqlClientAuthRepository;
    private final ClientService clientService;
    private final PlanService planService;
    private final AuthorizationSecurity authorizationSecurity;

    public GetClientUseCase(InMemoryClientRepository inMemoryClientRepository, ClientRepository sqlClientAuthRepository, ClientService clientService, PlanService planService, AuthorizationSecurity authorizationSecurity) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.clientService = clientService;
        this.planService = planService;
        this.authorizationSecurity = authorizationSecurity;
    }


    public List<Client> getAll() {
        return clientService.getAll();
    }



    public int getConnectedClientsAntenna(String uuid) {
        if (inMemoryClientRepository.getAllClients().isEmpty()) {
            getAll();
        }
        UUID antennaUuid = UUID.fromString(uuid);
        return inMemoryClientRepository.getConnectedClientsByAntenna(antennaUuid);
    }

    public int getAllClientConectedAntenna() {
        if (inMemoryClientRepository.getAllClients().isEmpty()) {
            getAll();
        }
        return inMemoryClientRepository.getAllConnectedClients();
    }

    public int getTotalClients() {
        if (inMemoryClientRepository.getAllClients().isEmpty()) {

        }
        return inMemoryClientRepository.getAllClients().size();
    }

    public long getClientsAlDay() {

        List<Client> clients = getAll();

        return getAll().stream()
                .filter(c -> c.getClientStatus() == ClientStatus.ACTIVO)
                .count();

    }

    public long getClientsPending() {
        return inMemoryClientRepository.getAllClients().stream()
                .filter(c -> c.getCountPendingPayments() > 0).count();
    }

    public Client getBestClient() {
        return inMemoryClientRepository.getAllClients().stream()
                .filter(c -> c.getPaymentsScore() != null)
                .max(Comparator.comparingInt(
                        c -> c.getPaymentsScore().getCountPaidPayments()
                ))
                .orElse(null);
    }

    public List<Client> getMostIrresponsibleClients() {
        return inMemoryClientRepository.getAllClients().stream()
                .filter(client -> client.getPaymentsScore() != null)
                .sorted(Comparator.comparingInt(
                        (Client client) -> client.getPaymentsScore()
                                .getPendingPayments().size()
                ).reversed())
                .toList();
    }

    public List<Client> searchClients(String searchType, String query) {

        if (!authorizationSecurity.hasPermission(Permission.CLIENT_SEARCH)
                && !authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException(
                    "No tienes el rol necesario para buscar clientes"
            );
        }

        List<Client> clients = getAll();

        if (query == null || query.isBlank()) {
            return clients;
        }

        String normalized = normalize(query);

        return clients.stream()
                .filter(client -> matchesSearch(client, searchType, normalized))
                .toList();
    }

    public List<Client> searchDeudoresClients(String searchType, String query) {

        if (!authorizationSecurity.hasPermission(Permission.CLIENT_SEARCH)
                && !authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException(
                    "No tienes el rol necesario para buscar clientes"
            );
        }

        List<Client> clients = getAll();

        String normalized = normalize(query == null ? "" : query);

        return clients.stream()
                .filter(client -> client.getClientStatus() == ClientStatus.SUSPENDIDO)
                .filter(client -> normalized.isEmpty()
                        || matchesSearch(client, searchType, normalized))
                .toList();
    }

    private boolean matchesSearch(
            Client client,
            String searchType,
            String query
    ) {
        return switch (searchType) {
            case "Dni" -> contains(client.getDni(), query);

            case "Nombre" -> contains(client.getFirstName(), query);

            case "Apellido" -> contains(client.getLastName(), query);

            case "Direccion" -> contains(client.getAddress(), query);

            case "Telefono" -> contains(client.getPhone(), query);

            case "Whatsapp" -> contains(client.getWhatsapp(), query);

            case "Plan" -> client.getOrder() != null
                    && client.getOrder().getPlanUuid() != null
                    && contains(
                    planService.getPlanById(client.getOrder().getPlanUuid()).getName(),
                    query
            );

            default -> false;
        };


    }


    private boolean contains(String value, String query) {
        return value != null && normalize(value).contains(query);
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(
                value.trim().toLowerCase(java.util.Locale.ROOT),
                java.text.Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");
    }
}
