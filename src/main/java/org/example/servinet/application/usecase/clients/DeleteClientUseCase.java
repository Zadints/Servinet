package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.PermissionException;

public class DeleteClientUseCase {
    private final InMemoryClientRepository inMemoryClientRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ClientRepository sqlClientAuthRepository;
    private final ActivityLogsService activityLogsService;
    public DeleteClientUseCase(InMemoryClientRepository inMemoryClientRepository, AuthorizationSecurity authorizationSecurity, ClientRepository sqlClientAuthRepository, ActivityLogsService activityLogsService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.authorizationSecurity = authorizationSecurity;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.activityLogsService = activityLogsService;
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
        activityLogsService.addLog(
                LogType.USER_DELETE,
                "Realizó una eliminación de servicio para " +
                client.getFullName() + " Con número de teléfono " + client.getPhone());
    }

    public Client getClient(String dni) {
        if (dni == null || inMemoryClientRepository.getAllClients().isEmpty()) {
            return null;
        }
        return inMemoryClientRepository.getClient(dni);
    }
}
