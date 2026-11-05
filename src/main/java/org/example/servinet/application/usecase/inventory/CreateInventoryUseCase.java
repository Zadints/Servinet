package org.example.servinet.application.usecase.inventory;
import org.example.servinet.application.dto.InventoryItem;
import org.example.servinet.application.port.out.sql.InventoryRepository;

import java.math.BigDecimal;
import java.util.*;

public class CreateInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public CreateInventoryUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public UUID createItem(InventoryItem item) {

        System.out.println("Nombre recibido: [" + item.name() + "]");
        System.out.println("Longitud: " +
                (item.name() == null ? "NULL" : item.name().length()));

        if (item == null) {
            throw new IllegalArgumentException("Debes ingresar un artículo válido");
        }



        if (isBlank(item.name()) || item.name().length() > 150) {
            throw new IllegalArgumentException("El nombre del artículo es obligatorio y no debe superar 150 caracteres");
        }

        if (item.description() != null && item.description().length() > 500) {
            throw new IllegalArgumentException("La descripción no debe superar 500 caracteres");
        }

        if (item.category() == null) {
            throw new IllegalArgumentException("Debes seleccionar una categoría");
        }

        if (item.brand() != null && item.brand().length() > 100) {
            throw new IllegalArgumentException("La marca no debe superar 100 caracteres");
        }

        if (item.model() != null && item.model().length() > 100) {
            throw new IllegalArgumentException("El modelo no debe superar 100 caracteres");
        }

        if (item.unit() == null || !switch (item.unit()) {
            case "UNIDAD", "METRO", "ROLLO", "CAJA", "PAQUETE" -> true;
            default -> false;
        }) {
            throw new IllegalArgumentException("Unidad de medida inválida");
        }

        validateAmount(item.minimumStock(), "El stock mínimo");
        validateAmount(item.purchasePrice(), "El precio de compra");

        if (item.recoverable() && !"UNIDAD".equals(item.unit())) {
            throw new IllegalArgumentException("Los equipos recuperables deben registrarse por unidad");
        }

        return inventoryRepository.createItem(item);
    }

    public int registerEquipments(
            UUID itemUuid,
            String input,
            UUID staffUuid
    ) {
        if (itemUuid == null) {
            throw new IllegalArgumentException("Artículo inválido");
        }

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes ingresar al menos un número de serie"
            );
        }

        List<String> serials = Arrays.stream(input.trim().split("[,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        Set<String> unique = new HashSet<>();

        for (String serial : serials) {
            if (!unique.add(serial.toUpperCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        "Número de serie duplicado: " + serial
                );
            }

            if (serial.length() > 100) {
                throw new IllegalArgumentException(
                        "Número de serie demasiado largo: " + serial
                );
            }
        }

        for (String serial : serials) {
            inventoryRepository.registerEquipment(
                    itemUuid,
                    serial,
                    staffUuid
            );
        }

        return serials.size();
    }

    private void validateAmount(BigDecimal value, String field) {
        if (value == null || value.signum() < 0 || value.scale() > 2 || value.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException(field + " debe ser un número válido no negativo con máximo 2 decimales");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
