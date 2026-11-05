package org.example.servinet.application.usecase.inventory;
import org.example.servinet.application.port.out.sql.InventoryRepository;

import java.util.UUID;

public class ManageInventoryEquipmentUseCase {
    private final InventoryRepository inventoryRepository;

    public ManageInventoryEquipmentUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void assignEquipment(UUID equipmentUuid, UUID staffUuid) {
        validateEquipment(equipmentUuid);
        validateStaff(staffUuid);

        inventoryRepository.assignEquipment(equipmentUuid, staffUuid);
    }

    public void installEquipment(
            UUID equipmentUuid,
            String clientDni,
            UUID orderUuid,
            UUID staffUuid
    ) {
        validateEquipment(equipmentUuid);
        validateStaff(staffUuid);

        if (clientDni == null || !clientDni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI del cliente debe contener 8 dígitos");
        }

        if (orderUuid == null) {
            throw new IllegalArgumentException("Debes seleccionar una orden de instalación");
        }

        inventoryRepository.installEquipment(
                equipmentUuid,
                clientDni,
                orderUuid,
                staffUuid
        );
    }

    public void recoverEquipment(
            UUID equipmentUuid,
            UUID staffUuid,
            String description
    ) {
        validateEquipment(equipmentUuid);
        validateStaff(staffUuid);
        validateDescription(description);

        inventoryRepository.recoverEquipment(
                equipmentUuid,
                staffUuid,
                description.trim()
        );
    }

    public void releaseEquipment(
            UUID equipmentUuid,
            UUID staffUuid,
            String description
    ) {
        validateEquipment(equipmentUuid);
        validateStaff(staffUuid);
        validateDescription(description);

        inventoryRepository.releaseEquipment(
                equipmentUuid,
                staffUuid,
                description.trim()
        );
    }

    private void validateEquipment(UUID equipmentUuid) {
        if (equipmentUuid == null) {
            throw new IllegalArgumentException("Debes seleccionar un equipo");
        }
    }

    private void validateStaff(UUID staffUuid) {
        if (staffUuid == null) {
            throw new IllegalArgumentException("Debes identificar al técnico responsable");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar una descripción");
        }

        if (description.length() > 500) {
            throw new IllegalArgumentException("La descripción no debe superar 500 caracteres");
        }
    }
}
