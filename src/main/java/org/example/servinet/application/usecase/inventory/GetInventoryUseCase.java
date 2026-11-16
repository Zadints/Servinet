package org.example.servinet.application.usecase.inventory;

import org.example.servinet.application.dto.*;
import org.example.servinet.application.port.out.sql.SqlInventoryRepository;

import java.util.List;
import java.util.UUID;

public class GetInventoryUseCase {
    private final SqlInventoryRepository sqlInventoryRepository;

    public GetInventoryUseCase(SqlInventoryRepository sqlInventoryRepository) {
        this.sqlInventoryRepository = sqlInventoryRepository;
    }

    public List<InventoryItem> getAllItems() {
        return sqlInventoryRepository.getAllItems();
    }

    public List<InventoryAlert> getStockAlerts() {
        return sqlInventoryRepository.getStockAlerts();
    }

    public List<InventoryMovement> getMovements(UUID itemUuid) {
        return sqlInventoryRepository.getMovements(itemUuid);
    }

    public List<InventoryEquipment> getTechnicianEquipment(UUID staffUuid) {
        if (staffUuid == null) {
            throw new IllegalArgumentException("Debes seleccionar un técnico");
        }

        return sqlInventoryRepository.getTechnicianEquipment(staffUuid);
    }

    public List<InventoryEquipment> getClientEquipment(String clientDni) {
        if (clientDni == null || !clientDni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe contener 8 dígitos");
        }

        return sqlInventoryRepository.getClientEquipment(clientDni);
    }

    public List<InventoryItem> searchItems(String query) {
        List<InventoryItem> items = sqlInventoryRepository.getAllItems();

        if (query == null || query.isBlank()) {
            return items;
        }

        String search = query.trim().toLowerCase(java.util.Locale.ROOT);

        return items.stream()
                .filter(item ->
                        contains(item.name(), search) ||
                                contains(item.code(), search) ||
                                contains(item.brand(), search) ||
                                contains(item.model(), search) ||
                                item.category() != null &&
                                        contains(item.category().getDisplayName(), search)
                )
                .toList();
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(java.util.Locale.ROOT).contains(search);
    }
}
