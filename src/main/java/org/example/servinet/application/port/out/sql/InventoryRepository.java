package org.example.servinet.application.port.out.sql;

import org.example.servinet.application.dto.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InventoryRepository {
    UUID createItem(InventoryItem item);
    void updateItem(InventoryItem item);
    List<InventoryItem> getAllItems();
    List<InventoryAlert> getStockAlerts();
    InventoryMovementResult registerMovement(UUID itemUuid, String movementType, BigDecimal quantity, UUID staffUuid, UUID orderUuid, String description);
    List<InventoryMovement> getMovements(UUID itemUuid);
    void deactivateItem(UUID itemUuid);
    UUID registerEquipment(UUID itemUuid, String serialNumber, UUID staffUuid);
    void assignEquipment(UUID equipmentUuid, UUID staffUuid);
    void installEquipment(UUID equipmentUuid, String clientDni, UUID orderUuid, UUID staffUuid);
    void recoverEquipment(UUID equipmentUuid, UUID staffUuid, String description);
    void releaseEquipment(UUID equipmentUuid, UUID staffUuid, String description);
    List<InventoryEquipment> getTechnicianEquipment(UUID staffUuid);
    List<InventoryEquipment> getClientEquipment(String clientDni);
    InventoryMovementResult withdrawEquipment(
            UUID itemUuid,
            int quantity,
            UUID staffUuid,
            String description
    );
}
