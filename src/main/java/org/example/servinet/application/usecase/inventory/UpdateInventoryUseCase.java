package org.example.servinet.application.usecase.inventory;
import org.example.servinet.application.dto.InventoryItem;
import org.example.servinet.application.dto.InventoryMovementResult;
import org.example.servinet.application.port.out.sql.InventoryRepository;

import java.math.BigDecimal;
import java.util.UUID;
public class UpdateInventoryUseCase {
    private final InventoryRepository inventoryRepository;

    public UpdateInventoryUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void updateItem(InventoryItem item) {
        if (item == null || item.uuid() == null) {
            throw new IllegalArgumentException("Debes seleccionar un artículo válido");
        }

        if (item.name() == null || item.name().isBlank() || item.name().length() > 150) {
            throw new IllegalArgumentException("El nombre del artículo es inválido");
        }

        if (item.description() != null && item.description().length() > 500) {
            throw new IllegalArgumentException("La descripción es demasiado larga");
        }

        if (item.category() == null) {
            throw new IllegalArgumentException("Debes seleccionar una categoría");
        }

        if (item.brand() != null && item.brand().length() > 100) {
            throw new IllegalArgumentException("La marca es demasiado larga");
        }

        if (item.model() != null && item.model().length() > 100) {
            throw new IllegalArgumentException("El modelo es demasiado largo");
        }

        if (item.minimumStock() == null || item.minimumStock().signum() < 0 ||
                item.minimumStock().scale() > 2 ||
                item.minimumStock().compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException("El stock mínimo es inválido");
        }

        if (item.purchasePrice() == null || item.purchasePrice().signum() < 0 ||
                item.purchasePrice().scale() > 2 ||
                item.purchasePrice().compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException("El precio de compra es inválido");
        }

        inventoryRepository.updateItem(item);
    }

    public InventoryMovementResult withdrawEquipment(
            UUID itemUuid,
            BigDecimal quantity,
            UUID staffUuid,
            String description
    ) {
        if (itemUuid == null) {
            throw new IllegalArgumentException("El artículo es obligatorio.");
        }

        if (quantity == null
                || quantity.signum() <= 0
                || quantity.stripTrailingZeros().scale() > 0
                || quantity.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {

            throw new IllegalArgumentException(
                    "La cantidad de equipos debe ser un número entero positivo."
            );
        }

        return inventoryRepository.withdrawEquipment(
                itemUuid,
                quantity.intValueExact(),
                staffUuid,
                description
        );
    }

    public void deactivateItem(UUID itemUuid) {
        if (itemUuid == null) {
            throw new IllegalArgumentException("Debes seleccionar un artículo");
        }

        inventoryRepository.deactivateItem(itemUuid);
    }
}
