package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.dto.OrderReportDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.infrastructure.database.sqlserver.SqlClientRepository;

import java.util.List;

public class GetClientUseCase {
    private final InMemoryClientRepository inMemoryClientRepository;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;

    public GetClientUseCase(InMemoryClientRepository inMemoryClientRepository,ClientRepository sqlClientAuthRepository, PaymentsService paymentsService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.paymentsService = paymentsService;
    }

    public void loadClientsAndOrders() {

        List<OrderReportDto> orders = sqlClientAuthRepository.getAllClientsOrderDatabase();

        for (OrderReportDto o : orders) {
            var client = new Client(
                    o.getClientDni(),
                    o.getClientEmail(),
                    o.getPlanName(),
                    o.getClientLastName(),
                    o.getClientPhone(),
                    o.getClientAddress(),
                    o.getOrderStartInstall(),
                    o.getOrderCreate(),
                    o.getClientStatus(),
                    o.getPaymentStatus(),
                    o.getImgRouter()
            );
            /*
            * Load the pays and plan
            **/
            client.setPayments(paymentsService.getAll(client.getDni()));
            //client.setPlan(PlanUseCase.getPlanById(o.getPlanId()));

            inMemoryClientRepository.addClient(client);
        }
    }

    public List<Client> getAllClients() {
        return inMemoryClientRepository.getAllClients() ;
    }

    public Client getClient(String dni) {
        if (dni == null || inMemoryClientRepository.getAllClients().isEmpty()) {
            return null;
        }
        return inMemoryClientRepository.getClient(dni);
    }


}
