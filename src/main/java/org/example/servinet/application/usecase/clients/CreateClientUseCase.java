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
import java.util.regex.Pattern;

public class CreateClientUseCase {

    private final InMemoryClientRepository inMemoryClientRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;
    private final ClientService clientService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern VALID_ADDRESS = Pattern.compile(
            "^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s.,#\\-/°º]+$"
    );

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

        checkDni(dto.dni());
        checkFullName(dto.lastName(), dto.firstName());
        checkPhone(dto.phone());
        checkAddress(dto.address());
        checkMail(dto.email());
        checkPhone(dto.whatsapp());

        if (dto.plan() == null)
            throw new InvalidValueException("Debes seleccionar un plan");
        checkCoordinates(dto.latitude(), dto.longitude());
        if (dto.antennaUuid() == null || dto.antennaUuid().isEmpty()){
            //redireccionar a la más recomendable mijin

        }


        if ( clientService.isClientExist(dto.dni())) {
            throw new InvalidValueException("Ya existe un cliente registrado con ese DNI");
        }

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
                PlanStatusClient.AL_DIA, //aclarado por Chinguel cualquier cosa yo no soy
                null,
                null,
                dto.longitude(),
                dto.latitude()
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

    

    private void checkDni(String dni){
        if (dni == null || dni.isBlank()) {
            throw new InvalidValueException("Debes ingresar el DNI del cliente");
        }

        if (!dni.matches("\\d{8}")) {
            throw new InvalidValueException("El DNI debe tener 8 dígitos");
        }

    }


    private void checkFullName(String lastName, String firstName){
        if (lastName == null || lastName.isBlank()) {
            throw new InvalidValueException("Debes ingresar los dos nombres");
        }

        if (firstName == null || firstName.isBlank()) {
            throw new InvalidValueException("Debes ingresar los dos apellidos");
        }

        if (!lastName.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new InvalidValueException("El nombre solo puede contener letras y espacios");
        }

        if (!firstName.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new InvalidValueException("El nombre solo puede contener letras y espacios");
        }
    }

    private void checkPhone(String phone){

        if (phone == null || phone.isBlank() || !phone.matches("\\d{9}")) {
            throw new InvalidValueException("El teléfono ingresado no es válido");
        }
    }
    private void checkAddress(String address){
        if (address == null || address.isBlank()) {
            throw new InvalidValueException("Debe ingresar una dirección válida");
        }

        if (address.length() < 8 || address.length() > 200) {
            throw new InvalidValueException("La dirección es muy extensa");
        }
        if (!VALID_ADDRESS.matcher(address).matches()) {
            throw new InvalidValueException("La dirección no tiene formato válido");
        }

        if (!address.matches(".*\\p{L}.*")) {
            throw new InvalidValueException("La dirección debe contener al menso una letra");
        }
        if (address.matches("(?i)(.)\\1{3,}")) {
            throw new InvalidValueException("La dirección está mal escrita");
        }
    }

    private void checkMail(String email){
        if (email == null || email.isBlank()) {
            throw new InvalidValueException("Debes ingresar un correo");
        }

        if(!EMAIL_PATTERN.matcher(email.trim()).matches()){
            throw new InvalidValueException("El correo ingresado es inválido");
        }
    }

    public static void checkCoordinates(
            Double latitude,
            Double longitude
    ) {

        if (latitude == null || longitude == null) {
            throw new InvalidValueException("Por favor selecciona en el mapa la ubicación aproximada de la casa");
        }

        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            throw new InvalidValueException("La latitud o longitud es infinita");
        }

        if (!(latitude >= -8.12 && latitude <= -7.96
                && longitude >= -79.08 && longitude <= -78.94)){
            throw new InvalidValueException("Las coordenadas ingresadas excede el límite del área de alcance");
        }
    }
}
