package org.example.servinet.core.application.usecase;

import org.example.servinet.core.application.dto.ClientCreateDto;
import org.example.servinet.core.application.dto.OrderReportDto;
import org.example.servinet.core.application.security.PermissionValidation;
import org.example.servinet.core.domain.entities.Client;
import org.example.servinet.core.domain.entities.ClientPlan;
import org.example.servinet.core.domain.enums.ClientStatus;
import org.example.servinet.core.domain.enums.Permission;
import org.example.servinet.core.domain.enums.PlanStatusClient;
import org.example.servinet.core.domain.exception.InvalidValueException;
import org.example.servinet.core.domain.exception.RoleNoPermission;
import org.example.servinet.infrastructure.database.models.ClientModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClientsUseCase {

    private static List<Client> listClients = new ArrayList<>();
    private static List<OrderReportDto> listOrdersClients = new ArrayList<>();

    public static void loadClients() {
        List<OrderReportDto> lstTemp = ClientModel.getAllClientsOrderDatabase();
        listOrdersClients = lstTemp;
        listClients.clear();

        PlanUseCase.loadAllPlans(lstTemp);

        // 2. Construimos la lista de clientes reutilizando o asignando su ClientPlan
        for (OrderReportDto o : lstTemp) {
            ClientPlan assignedPlan = PlanUseCase.getPlanById(o.getPlanId());
            if (assignedPlan == null) {
                assignedPlan = new ClientPlan(
                        o.getPlanId(),
                        o.getPlanName(),
                        o.getPlanSpeed(),
                        o.getPlanPrice(),
                        true
                );
            }

            listClients.add(new Client(
                    o.getClientDni(),
                    o.getClientEmail(),
                    o.getPlanName(),
                    o.getClientLastName(),
                    o.getClientPhone(),
                    o.getClientAddress(),
                    assignedPlan,
                    o.getOrderStartInstall(),
                    o.getOrderCreate(),
                    o.getClientStatus(),
                    o.getPaymentStatus(),
                    o.getImgRouter()
            ));
        }
    }

    public static List<Client> getAllClients() {
        return listClients;
    }

    public static Client getClient(String dni) {
        if (dni == null || listClients.isEmpty()) {
            return null;
        }

        return listClients.stream()
                .filter(c -> c.getDni().equals(dni))
                .findFirst()
                .orElse(null);
    }

    public static Client addClientOrder(ClientCreateDto dto) {
        if (!PermissionValidation.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new RoleNoPermission("No tienes el rol necesario para crear un cliente");
        }

        if (dto.getDni() == null || dto.getDni().isBlank()) {
            throw new InvalidValueException("Debes ingresar el DNI del cliente");
        }

        if (!dto.getDni().matches("\\d{8}")) {
            throw new InvalidValueException("El DNI debe tener 8 dígitos");
        }

        boolean dniInUse = listClients.stream()
                .anyMatch(c -> dto.getDni().equals(c.getDni()));
        if (dniInUse) {
            throw new InvalidValueException("Ya existe un cliente registrado con ese DNI");
        }

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
        if (phone == null || phone.isBlank() || !phone.matches("\\d{6,15}")) {
            throw new InvalidValueException("El teléfono ingresado no es válido");
        }

        String whatsApp = dto.getWhatsapp();
        if (whatsApp == null || whatsApp.isBlank()) {
            whatsApp = phone;
        } else if (!whatsApp.matches("\\d{6,15}")) {
            throw new InvalidValueException("El teléfono de WhatsApp ingresado no es válido");
        }

        // Validación delegada a PlanUseCase
        if (!PlanUseCase.existPlans(dto.getPlan())) {
            throw new InvalidValueException("Plan seleccionado inválido");
        }

        Client newClient = new Client(
                dto.getDni(),
                dto.getEmail(),
                dto.getFirstName().trim(),
                dto.getLastName() != null ? dto.getLastName().trim() : "",
                dto.getPhone(),
                dto.getAddress(),
                dto.getPlan(),
                null,
                LocalDateTime.now(),
                ClientStatus.ACTIVO,
                PlanStatusClient.AL_DIA,
                null
        );

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



        ClientModel.setOrderDatabase(newClient, whatsApp);
        listClients.add(0, newClient);

        return null;
    }

    public static Client editClient(ClientCreateDto dto) {
/*
        if (!PermissionValidation.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new RoleNoPermission("No tienes el rol necesario para editar un cliente");
        }

        if (dto.getUuid() == null) {
            throw new InvalidValueException("No se especificó qué cliente editar");
        }

        Client existing = getClient(dto.getUuid());

        if (existing == null) {
            throw new InvalidValueException("El cliente ya no existe");
        }

        validateClientData(dto);

        boolean dniUsedByAnother = listClients.stream()
                .anyMatch(c -> !c.getUuid().equals(dto.getUuid())
                        && dto.getDni() != null
                        && dto.getDni().equals(c.getDni()));

        if (dniUsedByAnother) {
            throw new InvalidValueException("Ya existe otro cliente registrado con ese DNI");
        }

        existing.setDni(dto.getDni());
        existing.setEmail(dto.getEmail());
        existing.setFirstName(dto.getFirstName().trim());
        existing.setLastName(dto.getLastName() != null ? dto.getLastName().trim() : "");
        existing.setPhone(dto.getPhone());
        existing.setZone(dto.getZone());
        existing.setAddress(dto.getAddress());
        existing.setInstallationDate(
                dto.getInstallationDate() != null ? dto.getInstallationDate().atStartOfDay() : null
        );

        if (dto.getClientStatus() != null) {
            existing.setClientStatus(dto.getClientStatus());
        }

        if (dto.getPlanId() != null && !dto.getPlanId().isBlank()) {
            existing.setPlan(new ClientPlan(dto.getPlanId(), dto.getPlanId(), null, null, true));
        }

        ClientModel.updateClientDatabase(existing);
*/
        return null; //existing;
    }

    public static void deleteClient(String dni) {
        if (!PermissionValidation.hasPermission(Permission.CLIENT_DELETE)
                && !PermissionValidation.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new RoleNoPermission("No tienes el rol necesario para eliminar clientes");
        }

        Client client = getClient(dni);
        if (client == null) {
            return;
        }

        ClientModel.deleteClientDatabase(client);
        listClients.remove(client);
    }

    public static List<Client> searchClients(String searchType, String query) {
        if (!PermissionValidation.hasPermission(Permission.CLIENT_SEARCH)
                && !PermissionValidation.hasPermission(Permission.CLIENTS_BYPASS)) {
            throw new RoleNoPermission("No tienes el rol necesario para buscar clientes");
        }

        if (query == null || query.isBlank()) {
            return listClients;
        }

        String normalized = query.trim().toLowerCase();

        return listClients.stream()
                .filter(c -> matchesSearch(c, searchType, normalized))
                .toList();
    }

    private static boolean matchesSearch(Client c, String searchType, String query) {
        if (searchType == null) {
            return matchesAnyField(c, query);
        }
        return switch (searchType) {
            case "Persona" -> (c.getFullName() != null && c.getFullName().toLowerCase().contains(query))
                    || (c.getDni() != null && c.getDni().toLowerCase().contains(query))
                    || (c.getPhone() != null && c.getPhone().toLowerCase().contains(query));
            case "Dirección" -> c.getAddress() != null && c.getAddress().toLowerCase().contains(query);
            default -> matchesAnyField(c, query);
        };
    }

    private static boolean matchesAnyField(Client c, String query) {
        return (c.getFullName() != null && c.getFullName().toLowerCase().contains(query))
                || (c.getDni() != null && c.getDni().toLowerCase().contains(query))
                || (c.getPhone() != null && c.getPhone().toLowerCase().contains(query))
                || (c.getAddress() != null && c.getAddress().toLowerCase().contains(query));
    }

    public static int getTotalClients() {
        return listClients.size();
    }

    public static long getClientsUpToDate() {
        return listClients.stream().filter(c -> c.getPlanStatus() == PlanStatusClient.AL_DIA).count();
    }

    public static long getClientsPending() {
        return listClients.stream().filter(c -> c.getPlanStatus() == PlanStatusClient.CON_DEUDA).count();
    }

    public static Client getBestClient() {
        return listClients.stream()
                .max(Comparator.comparingInt(Client::getScorePayment))
                .orElse(null);
    }

    public static List<Client> getMostIrresponsibleClients() {
        return listClients.stream()
                .sorted(Comparator.comparingInt(Client::getScorePayment))
                .toList();
    }
}