package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.dto.ClientCreateDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.PaymentsService;
import org.example.servinet.application.usecase.PlanUseCase;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.enums.ClientStatus;
import org.example.servinet.domain.enums.Permission;
import org.example.servinet.domain.enums.PlanStatusClient;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PermissionException;
import org.example.servinet.infrastructure.database.sqlserver.SqlClientRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateClientUseCase {

    private final InMemoryClientRepository inMemoryClientRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;

    public CreateClientUseCase(InMemoryClientRepository inMemoryClientRepository, AuthorizationSecurity authorizationSecurity, ClientRepository sqlClientAuthRepository, PaymentsService paymentsService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.authorizationSecurity = authorizationSecurity;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.paymentsService = paymentsService;
    }

    public Client addClientOrder(ClientCreateDto dto) {
        if (!authorizationSecurity.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new PermissionException("No tienes el rol necesario para crear un cliente");
        }

        if (dto.getDni() == null || dto.getDni().isBlank()) {
            throw new InvalidValueException("Debes ingresar el DNI del cliente");
        }

        if (!dto.getDni().matches("\\d{8}")) {
            throw new InvalidValueException("El DNI debe tener 8 dígitos");
        }

        Client dniInUse = inMemoryClientRepository.getClient(dto.getDni());
        if (dniInUse != null) {
            throw new InvalidValueException("Ya existe un cliente registrado con ese DNI");
        }

        String whatsApp = checkDtoClient(dto);

        Client newClient = new Client(
                dto.getDni(),
                dto.getEmail(),
                dto.getFirstName().trim(),
                dto.getLastName().trim(),
                dto.getPhone(),
                dto.getAddress(),
                null,
                LocalDateTime.now(),
                ClientStatus.ACTIVO,
                PlanStatusClient.AL_DIA,
                null
        );
        newClient.setPlan(dto.getPlan());
        paymentsService.createNewPay(newClient);
        //newClient.addNewPayments(paymentsService.getAll(dto.getDni()));

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



        sqlClientAuthRepository.setOrderDatabase(newClient, whatsApp);
        inMemoryClientRepository.addClient( newClient);

        return null;
    }


    private static String checkDtoClient(ClientCreateDto dto){
        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            throw new InvalidValueException("Debes ingresar el nombre del cliente");
        }

        if (!dto.getFirstName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new InvalidValueException("El nombre solo puede contener letras y espacios");
        }

        if (dto.getLastName() != null && !dto.getLastName().isBlank()
                && !dto.getLastName().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new InvalidValueException("El apellido solo puede contener letras y espacios");
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidValueException("El correo ingresado no es válido");
        }

        String phone = dto.getPhone();
        if (phone == null || phone.isBlank() || !phone.matches("\\d{9}")) {
            throw new InvalidValueException("El teléfono ingresado no es válido");
        }

        String whatsApp = dto.getWhatsapp();
        if (whatsApp == null || whatsApp.isBlank()) {
            whatsApp = phone;
        } else if (!whatsApp.matches("\\d{6,15}")) {
            throw new InvalidValueException("El teléfono de WhatsApp ingresado no es válido");
        }


        return whatsApp;
    }
}
