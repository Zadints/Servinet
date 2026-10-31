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

    public List<OrderReportDto> getOrdersByTechnician(String uuidTechnician) {
        return inMemoryClientRepository.getOrdersByTechnician(uuidTechnician);
    }

}