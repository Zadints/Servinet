
package org.example.servinet.application.usecase.inventory;

import org.example.servinet.application.dto.InventoryItem;
import org.example.servinet.application.dto.InventoryMovementResult;
import org.example.servinet.application.port.out.sql.InventoryRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class ManageInventoryStockUseCase {

    private final InventoryRepository inventoryRepository;

    public ManageInventoryStockUseCase(
            InventoryRepository inventoryRepository
    ) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryMovementResult registerEntry(
            UUID itemUuid,
            BigDecimal quantity,
            UUID staffUuid,
            String description
    ) {
        validateMovement(itemUuid, quantity, staffUuid);
        validateConsumable(itemUuid);

        return inventoryRepository.registerMovement(
                itemUuid,
                "ENTRADA",
                quantity,
                staffUuid,
                null,
                description
        );
    }

    public InventoryMovementResult registerExit(
            UUID itemUuid,
            BigDecimal quantity,
            UUID staffUuid,
            UUID orderUuid,
            String description
    ) {
        validateMovement(itemUuid, quantity, staffUuid);

        InventoryItem item = getItem(itemUuid);

        if (item.stock().compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Stock insuficiente. Disponible: "
                            + item.stock().stripTrailingZeros().toPlainString()
                            + " " + item.unit()
            );
        }

        // Equipos recuperables: retirar automáticamente
        // la cantidad solicitada de UUID disponibles.
        if (item.recoverable()) {

            if (quantity.stripTrailingZeros().scale() > 0) {
                throw new IllegalArgumentException(
                        "La cantidad de equipos debe ser un número entero"
                );
            }

            if (quantity.compareTo(
                    BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
                throw new IllegalArgumentException(
                        "La cantidad de equipos supera el límite permitido"
                );
            }

            if (orderUuid != null) {
                throw new IllegalArgumentException(
                        "La salida automática de equipos no admite una orden"
                );
            }

            return inventoryRepository.withdrawEquipment(
                    itemUuid,
                    quantity.intValueExact(),
                    staffUuid,
                    description
            );
        }

        // Materiales consumibles: salida normal.
        return inventoryRepository.registerMovement(
                itemUuid,
                "SALIDA",
                quantity,
                staffUuid,
                orderUuid,
                description
        );
    }

    public InventoryMovementResult withdrawEquipment(
            UUID itemUuid,
            BigDecimal quantity,
            UUID staffUuid,
            String description
    ) {
        validateMovement(itemUuid, quantity, staffUuid);

        InventoryItem item = getItem(itemUuid);

        if (!item.recoverable()) {
            throw new IllegalArgumentException(
                    "Este artículo no es un equipo recuperable"
            );
        }

        if (quantity.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(
                    "La cantidad de equipos debe ser un número entero"
            );
        }

        if (quantity.compareTo(
                BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException(
                    "La cantidad de equipos supera el límite permitido"
            );
        }

        if (item.stock().compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Stock insuficiente. Disponible: "
                            + item.stock().stripTrailingZeros().toPlainString()
            );
        }

        return inventoryRepository.withdrawEquipment(
                itemUuid,
                quantity.intValueExact(),
                staffUuid,
                description
        );
    }

    public InventoryMovementResult registerAdjustment(
            UUID itemUuid,
            BigDecimal quantity,
            UUID staffUuid,
            String description
    ) {
        validateMovement(itemUuid, quantity, staffUuid);
        validateConsumable(itemUuid);

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar el motivo del ajuste"
            );
        }

        return inventoryRepository.registerMovement(
                itemUuid,
                "AJUSTE",
                quantity,
                staffUuid,
                null,
                description
        );
    }

    private void validateMovement(
            UUID itemUuid,
            BigDecimal quantity,
            UUID staffUuid
    ) {
        if (itemUuid == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un artículo"
            );
        }

        if (staffUuid == null) {
            throw new IllegalArgumentException(
                    "Debes identificar al responsable del movimiento"
            );
        }

        if (quantity == null
                || quantity.signum() <= 0
                || quantity.stripTrailingZeros().scale() > 2
                || quantity.compareTo(
                new BigDecimal("9999999999.99")) > 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero, con máximo 2 decimales"
            );
        }
    }

    private InventoryItem getItem(UUID itemUuid) {
        return inventoryRepository.getAllItems()
                .stream()
                .filter(item -> itemUuid.equals(item.uuid()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El artículo no existe o está inactivo"
                        )
                );
    }

    private void validateConsumable(UUID itemUuid) {
        InventoryItem item = getItem(itemUuid);

        if (item.recoverable()) {
            throw new IllegalArgumentException(
                    "Los equipos recuperables deben registrarse "
                            + "mediante su número de serie"
            );
        }
    }
}
