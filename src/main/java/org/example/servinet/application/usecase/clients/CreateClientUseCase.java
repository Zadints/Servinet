package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.dto.ClientDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.ClientService;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;

import java.time.LocalDateTime;

public class CreateClientUseCase {

    private final InMemoryClientRepository inMemoryClientRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;
    private final ClientService clientService;

    public CreateClientUseCase(InMemoryClientRepository inMemoryClientRepository, AuthorizationSecurity authorizationSecurity, ClientRepository sqlClientAuthRepository, PaymentsService paymentsService, ClientService clientService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.authorizationSecurity = authorizationSecurity;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.paymentsService = paymentsService;
        this.clientService = clientService;
    }

    public void addClientOrder(ClientDto dto) {
        if (!authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException("No tienes el rol necesario para crear un cliente");
        }
        checkDtoClient(dto);

        Client newClient = new Client(
                dto.dni(),
                dto.email(),
                dto.firstName().trim(),
                dto.lastName().trim(),
                dto.phone(),
                dto.whatsapp(),
                dto.address(),
                dto.plan(),
                null,
                LocalDateTime.now(),
                ClientStatus.ACTIVO,
                PlanStatusClient.AL_DIA,
                null,
                null
        );

        paymentsService.createNewPay(newClient);
        newClient.setPayments(paymentsService.getAll(dto.dni()));

        /*
         * Para crear un registroen db se nesecita un monto :v por ello saque asi
         * @DTO_GLOBAL ClientCreateDto => transporta datos de ui del form de Luis a el backend.
         * @entity Client => objeto completo del cliente que se gaurdará en memoria para
         *   poder usarlo luego para renderiar al gusto
         * @DTO_GLOBAL OrderReport => el get con innerJOIn de db que trae todos los datos
         *   volcados y debe armarse las entidades correspondientes para guardar en este usecase u otro.
         *   Falta ordenar mejro esta parte porque volca demasiados datos.
         *
         *
         */



        sqlClientAuthRepository.setOrderDatabase(newClient, dto.antennaUuid());
        inMemoryClientRepository.addClient(newClient);

    }


    private void checkDtoClient(ClientDto dto){

        if (dto.dni() == null || dto.dni().isBlank()) {
            throw new InvalidValueException("Debes ingresar el DNI del cliente");
        }

        if (!dto.dni().matches("\\d{8}")) {
            throw new InvalidValueException("El DNI debe tener 8 dígitos");
        }

        if ( clientService.isClientExist(dto.dni())) {
            throw new InvalidValueException("Ya existe un cliente registrado con ese DNI");
        }

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
    }
}
