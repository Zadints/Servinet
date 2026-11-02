package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.dto.ClientDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;

public class EditClientUseCase {
    private final InMemoryClientRepository inMemoryClientRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;

    public EditClientUseCase(InMemoryClientRepository inMemoryClientRepository, AuthorizationSecurity authorizationSecurity, ClientRepository sqlClientAuthRepository, PaymentsService paymentsService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.authorizationSecurity = authorizationSecurity;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.paymentsService = paymentsService;
    }


    public void editClient(ClientDto dto) {

        if (!authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException("No tienes el rol necesario para editar un cliente");
        }

        if (dto.dni() == null) {
            throw new InvalidValueException("No se especificó qué cliente editar");
        }

        Client existing = getClient(dto.dni());

        if (existing == null) {
            throw new InvalidValueException("El cliente no existe");
        }

        String  whatsApp = checkDtoClient(dto);


        existing.setFirstName(dto.firstName().trim());
        existing.setLastName(dto.lastName().trim());
        existing.setPhone(dto.phone());
        existing.setAddress(dto.address());
        existing.setEmail(dto.email());
        existing.setPlan(dto.plan());

        sqlClientAuthRepository.updateClientDatabase(existing, whatsApp);
        inMemoryClientRepository.editClient(existing);
    }

    public Client getClient(String dni) {
        if (dni == null || inMemoryClientRepository.getAllClients().isEmpty()) {
            return null;
        }
        return inMemoryClientRepository.getClient(dni);
    }

    private static String checkDtoClient(ClientDto dto){
        if (dto.firstName() == null || dto.firstName().isBlank()) {
            throw new InvalidValueException("Debes ingresar el nombre del cliente");
        }

        if (!dto.firstName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new InvalidValueException("El nombre solo puede contener letras y espacios");
        }

        if (dto.lastName() != null && !dto.lastName().isBlank()
                && !dto.lastName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new InvalidValueException("El apellido solo puede contener letras y espacios");
        }

        if (dto.email() != null && !dto.email().isBlank()
                && !dto.email().matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidValueException("El correo ingresado no es válido");
        }

        String phone = dto.phone();
        if (phone == null || phone.isBlank() || !phone.matches("\\d{9}")) {
            throw new InvalidValueException("El teléfono ingresado no es válido");
        }

        String whatsApp = dto.whatsapp();
        if (whatsApp == null || whatsApp.isBlank()) {
            whatsApp = phone;
        } else if (!whatsApp.matches("\\d{6,15}")) {
            throw new InvalidValueException("El teléfono de WhatsApp ingresado no es válido");
        }

        return whatsApp;
    }
}
