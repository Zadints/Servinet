package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.dto.ClientDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ActivityLogsService;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.enums.LogType;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;

public class EditClientUseCase {
    private final InMemoryClientRepository inMemoryClientRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ClientRepository sqlClientAuthRepository;
    private final ActivityLogsService activityLogsService;
    public EditClientUseCase(InMemoryClientRepository inMemoryClientRepository, AuthorizationSecurity authorizationSecurity, ClientRepository sqlClientAuthRepository, ActivityLogsService activityLogsService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.authorizationSecurity = authorizationSecurity;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.activityLogsService = activityLogsService;
    }

    public void editClient(ClientDto dto) {
        validateEditPermission();
        validateClientDto(dto);

        String whatsapp = resolveWhatsApp(dto);

        Client updated = new Client(
                dto.dni(),
                null, // firstName
                null, // lastName
                dto.phone(),
                dto.address() == null ? null : dto.address().trim(),
                normalizeEmail(dto.email()),
                whatsapp,
                null, // clientRegister
                null, // clientStatus
                null, // latitude
                null, // longitude
                null, // order
                null  // paymentsScore
        );

        sqlClientAuthRepository.updateClientDatabase(updated);
        inMemoryClientRepository.editClient(updated);
        activityLogsService.addLog(
                LogType.USER_DELETE,
                "Editó la información de un cliente con dni" +
                        dto.dni() + " Con número de teléfono " + dto.phone());
    }

    private void validateEditPermission() {
        if (!authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException(
                    "No tienes el rol necesario para editar un cliente"
            );
        }
    }

    private void validateClientDto(ClientDto dto) {
        if (dto == null) {
            throw new InvalidValueException("Los datos del cliente son obligatorios");
        }

        if (dto.dni() == null || dto.dni().isBlank()) {
            throw new InvalidValueException("No se especificó qué cliente editar");
        }

        validatePhone(dto.phone());
        validateEmail(dto.email());
        validateAddress(dto.address());
        validatePhone(dto.whatsapp());

    }

    private void validatePhone(String phone) {
        if (phone == null || !phone.matches("\\d{9}")) {
            throw new InvalidValueException("El teléfono ingresado no es válido");
        }
    }

    private void validateEmail(String email) {
        if (email != null && !email.isBlank()
                && !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidValueException("El correo ingresado no es válido");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new InvalidValueException("Debes ingresar la dirección del cliente");
        }
    }

    private String resolveWhatsApp(ClientDto dto) {
        String whatsapp = dto.whatsapp();

        if (whatsapp == null || whatsapp.isBlank()) {
            return dto.phone();
        }

        whatsapp = whatsapp.trim();

        if (!whatsapp.matches("\\d{9}")) {
            throw new InvalidValueException("El WhatsApp ingresado no es válido");
        }

        return whatsapp;
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim();
    }
}
