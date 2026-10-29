package org.example.servinet.application.service;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.PermissionException;

import java.util.Comparator;
import java.util.List;

public class ClientService {

    private final InMemoryClientRepository inMemoryClientRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;

    public ClientService(InMemoryClientRepository inMemoryClientRepository, AuthorizationSecurity authorizationSecurity, ClientRepository sqlClientAuthRepository, PaymentsService paymentsService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.authorizationSecurity = authorizationSecurity;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.paymentsService = paymentsService;
    }

    public Client getClient(String dni) {
        if (dni == null || inMemoryClientRepository.getAllClients().isEmpty()) {
            return null;
        }
        return inMemoryClientRepository.getClient(dni);
    }

    public void deleteClient(String dni) {
        if (!authorizationSecurity.hasPermission(Permission.CLIENT_DELETE)
                && !authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException("No tienes el rol necesario para eliminar clientes");
        }

        Client client = getClient(dni);
        if (client == null) {
            return;
        }

        sqlClientAuthRepository.deleteClientDatabase(client);
        inMemoryClientRepository.removeClient(client);
    }

    public List<Client> searchClients(String searchType, String query) {
        if (!authorizationSecurity.hasPermission(Permission.CLIENT_SEARCH)
                && !authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException("No tienes el rol necesario para buscar clientes");
        }

        if (query == null || query.isBlank()) {
            return inMemoryClientRepository.getAllClients();
        }

        String normalized = query.trim().toLowerCase();

        return inMemoryClientRepository.getAllClients().stream()
                .filter(c -> matchesSearch(c, searchType, normalized))
                .toList();
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

    public  int getTotalClients() {
        return inMemoryClientRepository.getAllClients().size();
    }

    public long getClientsUpToDate() {
        return inMemoryClientRepository.getAllClients().stream().filter(c -> c.getPlanStatus() == PlanStatusClient.AL_DIA).count();
    }

    public long getClientsPending() {
        return inMemoryClientRepository.getAllClients().stream().filter(c -> c.getPlanStatus() == PlanStatusClient.CON_DEUDA).count();
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


    public List<OrderReportDto> getOrdersByTechnician(String uuidTechnician) {
        return inMemoryClientRepository.getOrdersByTechnician(uuidTechnician);
    }

}