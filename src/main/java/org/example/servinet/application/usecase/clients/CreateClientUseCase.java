package org.example.servinet.application.usecase.clients;

import org.example.servinet.application.dto.AttentionLog;
import org.example.servinet.application.dto.ClientDto;
import org.example.servinet.application.port.out.InMemoryClientRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.service.*;
import org.example.servinet.application.usecase.antenna.GetAntennaUseCase;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.domain.entities.client.Client;
import org.example.servinet.domain.entities.client.Order;
import org.example.servinet.domain.entities.client.Payment;
import org.example.servinet.domain.enums.*;
import org.example.servinet.domain.exception.InvalidValueException;
import org.example.servinet.domain.exception.PaidException;
import org.example.servinet.domain.exception.PermissionException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

public class CreateClientUseCase {

    private final InMemoryClientRepository inMemoryClientRepository;
    private final AuthorizationSecurity authorizationSecurity;
    private final ClientRepository sqlClientAuthRepository;
    private final PaymentsService paymentsService;
    private final ClientService clientService;
    private final AntennaService antennaService;
    private final ClientContractService clientContractService;
    private final SellerService sellerService;
    private final StaffCacheService staffCacheService;
    private final ActivityLogsService activityLogsService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern VALID_ADDRESS = Pattern.compile(
            "^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s.,#\\-/°º]+$"
    );


    public CreateClientUseCase(InMemoryClientRepository inMemoryClientRepository, AuthorizationSecurity authorizationSecurity, ClientRepository sqlClientAuthRepository, PaymentsService paymentsService, ClientService clientService, AntennaService antennaService, ClientContractService clientContractService, SellerService sellerService, StaffCacheService staffCacheService, ActivityLogsService activityLogsService) {
        this.inMemoryClientRepository = inMemoryClientRepository;
        this.authorizationSecurity = authorizationSecurity;
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.paymentsService = paymentsService;
        this.clientService = clientService;
        this.antennaService = antennaService;
        this.clientContractService = clientContractService;
        this.sellerService = sellerService;
        this.staffCacheService = staffCacheService;
        this.activityLogsService = activityLogsService;
    }

    public void addClientOrder(ClientDto dto) throws InvalidValueException, PaidException {

        checkDni(dto.dni());
        checkFullName(dto.lastName(), dto.firstName());
        checkPhone(dto.phone());
        checkAddress(dto.address());
        checkMail(dto.email());
        checkPhone(dto.whatsapp());
        checkCoordinates(dto.latitude(), dto.longitude());

        System.out.println("Llegamos a validar todo el usuario");
        if (dto.plan() == null)
            throw new InvalidValueException("Debes seleccionar un plan");

        if (dto.antennaUuid() == null || dto.antennaUuid().isEmpty()){
            throw new InvalidValueException("Debes seleccionar una antenna o dejar que el app lo empareje");
        }

        if (clientService.isClientExist(dto.dni())) {
            throw new InvalidValueException("Ya existe un cliente registrado con ese DNI");
        }

        LocalDateTime createAt = LocalDateTime.now();
        String orderUuid = UUID.randomUUID().toString();
        Order order = new Order(
                orderUuid,
                dto.antennaUuid(),
                dto.plan().getId(),
                createAt,
                OrderStatus.PENDING
        );
        Client client = new Client(
                dto.dni(),
                dto.firstName(),
                dto.lastName(),
                dto.phone(),
                dto.address(),
                dto.email(),
                dto.whatsapp(),
                createAt,
                ClientStatus.EN_INSTALACION,
                dto.longitude(),
                dto.latitude(),
                order,
                null

        );


        System.out.println("Creando cliente en sql");
        //trae el client todo menos orders podria gregarse pero ns el tema de
        //solo apsamos client + order y pays se registra debajo
        sqlClientAuthRepository.createClientAndOrder(client, order);
        System.out.println("Creando cliente en mamory");
        inMemoryClientRepository.addClient(client);


        System.out.println("sumando a la antenna");
        //antenna service llamar para conectar a antenna aqui y sumar
        // +1 a la entidad y sql nd mas agregar
        antennaService.addClientCountAntenna(dto.antennaUuid(), dto.dni());


        Payment payment = new Payment(
                UUID.randomUUID().toString(),
                dto.dni(),
                BigDecimal.valueOf(dto.plan().getPrice()),
                createAt,
                createAt,
                PayMethods.EFECTIVO,
                true,
                null,
                "El pago de inicio de contrato es free",
                "Inicio de contrato"
        );
        System.out.println("Creando Pago en sql");

        paymentsService.savePaymentIfNoExist(payment, client);
        System.out.println("Enviando mail");
        clientContractService.sendMailClientContract(dto);

        String messageLog = "Realizó una venta del plan "+ dto.plan().getName() +" a el usuario " + client.getFirstName() +
                " con dni " + client.getDni() + " por una cantidad de S/." + dto.plan().getPrice();
        System.out.println("Creando registro de seller en sql");
        sellerService.register(new AttentionLog(
                staffCacheService.getCurrent().getUuid(),
                client.getDni(),
                client.getFirstName(),
                client.getLastName(),
                AttentionType.VENTA,
                5,
                messageLog
                ));
        System.out.println("Creando log final");
        activityLogsService.addLog(LogType.SELL, messageLog);
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
