package org.example.servinet.application.service;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.ClientPlan;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.PermissionException;

import java.util.*;

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

    public List<OrderReportDto> getOrdersByTechnician(String uuidTechnician) {
        return inMemoryClientRepository.getOrdersByTechnician(uuidTechnician);
    }

    public boolean isClientExist(String dni){

        List<Client> cachedClients = inMemoryClientRepository.getAllClients();

        if (cachedClients == null || cachedClients.isEmpty()) {
            cachedClients = getAll();
        }

        if (cachedClients == null || cachedClients.isEmpty()) return false;

        return inMemoryClientRepository.getClient(dni) != null;
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
                    null,
                    null,
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



}